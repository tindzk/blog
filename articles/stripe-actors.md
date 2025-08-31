+++
title       = "Using actors for more robust Stripe integrations"
published   = 2025-08-31
language    = "en-GB"
categories  = ["distributed-systems"]
description = "Applying the actor model to billing for a scalable and fault-tolerant Stripe integration"
+++

# Introduction
A fault-tolerant Stripe integration is harder to achieve than one might expect. Users may retry an ongoing operation, payments can fail, and the state between the client, server, database and Stripe may go out of sync, even if only momentarily. These are only some of the scenarios that will occur in production and must be planned for during development.

In this article, I argue that the reliability of a billing integration depends on its architecture. After spending considerable time modelling and testing billing scenarios, we found the actor model to be particularly well-suited.

# Motivation
Billing is an inherently asynchronous domain. Stripe partly abstracts away the asynchronicity by providing a CRUD interface with functions such as `Subscription.create` and `Subscription.list`, which return a current snapshot of these entities.

Yet, payment entities inevitably change over time; when subscriptions are involved, clients are billed on a schedule. Depending on the payment method, settlement may be delayed, as with SEPA. To support these scenarios, Stripe uses webhooks to send billing notifications.

As a side effect of webhooks, we will receive the result of certain operations from two sources: `Subscription.create` returns the created subscription, but we will also receive a webhook event indicating subscription creation. The order in which the results arrive is not guaranteed. Furthermore, other events related to the billing flow may be sent before the subscription creation event.

This means that the implementation needs to support both scenarios: `Subscription.create` responding first, and the webhook event arriving first. Similarly, we need to expect to receive other billing events, notably payment intents, _while_ the subscription is being set up.

Stripe sends HTTPS webhook requests whenever an event occurs. We can expect certain events to be delivered with a delay, or to be missed due to network partitions or during the deployment of a new server version.

Webhook events may originate not only from actions triggered by our server. Scheduled payments and manual changes in the Stripe dashboard will also trigger actions. Our implementation will need to be able to handle events for actions that our system did not initiate.

These observations show that a fault-tolerant implementation needs to account for non-deterministic event ordering, as well as missed or unexpected events. Considering all possible permutations is a daunting task, but starting from common failure scenarios and breaking them down into constituents (entities, API calls, webhook events) already goes a long way in guiding the implementation.

# Billing actors
If we frame payment processing in the context of distributed systems, this can drastically simplify how we approach the integration.

The main paradigm shift is to view each billing user as a separate actor whose state is modified by incoming messages. The state is entirely local to the actor and not modified externally.

## State
The actor state could look as follows in pseudocode:

```rust
type UserBillingState = {
  // name, email, ...
  userInfo: Opt<UserInfo>,

  // Free | Basic | Premium
  plan: Opt<UserPlan>,

  // set only if there is a subscription
  cancelled: Opt<bool>,

  // null if unknown
  userId: Opt<Str>,
  stripeCustomerId: Opt<Str>,
  stripeSubscriptionId: Opt<Str>,

  // Noop | TriggerPlanChange | ChangingPlan
  action: Action
}
```

All fields except the current action are optional. Due to non-deterministic ordering and missed events, we are potentially dealing with incomplete information. This design allows the actor to incrementally build a view of the billing state as it receives messages.

The definition lacks detailed customer and subscription information in favour of the corresponding entity IDs. This is deliberate because in distributed systems, entities are owned by a single service. Other services typically keep unique references to these entities. Since Stripe is responsible for billing customers, we treat it as the ground truth for all entities involved in the payment lifecycle. We retrieve the latest entity state only if needed for an action. This prevents acting on stale state, albeit at the cost of slightly higher latency.

## Messages
Next, we model all possible billing events that can occur in the system:

```typescript
type UserBillingEvent =
  | CustomerCreated       { /* ... */ }
  | ReceivedPaymentIntent { /* ... */ }
  | SubscriptionCancelled { /* ... */ }
  | SubscriptionFound     { /* ... */ }
  | SubscriptionNotFound  { /* ... */ }
  // ...
```

An incoming webhook event is then translated into the corresponding domain event before being passed to the actor for processing.

## Commands
The final step is to define commands such as `createCustomer`, `deleteSubscription` or `changePlan` that abstract the Stripe API calls. These functions are self-contained and accept all necessary inputs as parameters. They do not modify the actor state directly, but instead emit events:

```rust
fn createCustomer(
  actor: Actor<UserBillingEvent>,
  userId: Str,
  name: Str,
  email: Str,
  testClock: Opt<Str>
) {
  spawn {
    metadata := {"id": userId}
    customer := await stripe.Customer.create(name, email, testClock, metadata)
    actor ! CustomerCreated { stripeCustomerId: customer.id }
  }
}
```

When creating the customer, we attach the user's uniquely identifiable ID as metadata to support reverse lookups.

`createSubscription` is implemented similarly to the previous function:

```rust
fn createSubscription(
  actor: Actor<UserBillingEvent>,
  customerId: Str,
  priceId: Str,
  currency: Str
) {
  spawn {
    subscription := await stripe.Subscription.create(/* ... */);
    actor ! SubscriptionCreated { subscriptionId: subscription.id, status: subscription.status }
  }
}
```

We could have ignored the result and waited for the `customer.subscription.created` webhook event, but by turning the endpoint's result into a domain event, we can handle subscription creation as early as possible. Since actors are designed to be idempotent, the extraneous `SubscriptionCreated` message will have no effect.

Another notable feature of these functions is the use of `spawn`: We conceptually treat these functions as _commands_ that initiate an action. The action is performed in the background, and any associated notifications are received as messages.

If we expect many API calls, it would be preferable to store these commands in a queue to be processed according to Stripe's rate limits. Then, in place of calling `createCustomer(...)`, we would add a `CreateCustomer { ... }` command to the queue. The actions would be executed in sequence with the additional benefit of avoiding race conditions.

# State management
Knowing which information to store where and at which granularity in a distributed system is challenging. There is ultimately a trade-off between state freshness and performance.

## Architecture
State arises in three places: in-memory actors, persistent storage and Stripe.

When an action is triggered, missing data in the actor state is populated from the persistent storage and from Stripe as a fallback. This two-stage process reduces API calls and improves the execution time of billing actions since Stripe calls often have high response times.

The actor state contains transient information which is safe to lose. When the actor system is restarted, it begins with an empty state, which is populated on demand as actions take place.

The persistent storage is a relational database that durably maintains references to Stripe entities (customer ID, subscription ID) along with other frequently accessed data (user plan).

Stripe is the ground truth for all payment-related data. Name and email are set so that customers can be identified in case of data loss. The user's public ID is attached as metadata, which allows efficient reverse lookups when receiving webhook events.

## State drift
We cannot ensure that database references to Stripe entities are up to date at all times. State drift, meaning that the local state lags behind Stripe, can happen in various situations, for example, when restoring a backup or when multiple QA instances share the same user IDs. Similarly, when the backend is temporarily unreachable, Stripe cannot deliver webhooks, and the local state falls behind.

We design the actor logic to gracefully handle state drift. Consider the following scenario:

- The database indicates that the user is on the Free plan
- In Stripe, there is an active subscription for the Premium plan
- The user attempts to upgrade to the Basic plan

One approach would be to trigger an error. We solved this by transitioning the actor to the Premium plan and correcting the plan in the database. Although the sudden transition from Free to Premium may be surprising, the benefit is that the inconsistency is resolved automatically, and no manual intervention is required.

# Message processing
Two considerations apply when handling incoming billing messages in the actor: Some messages cannot be processed immediately, or should not lead to an action at all.

## Deferred processing
Messages may need to be deferred when waiting for information before the action can be executed. As events are immutable messages, they can be postponed by pushing them into an actor-local queue. After the expected message arrives, the deferred messages can be executed.

## Idempotency
We would like most actions to be idempotent, as clients might retry requests. Rather than triggering an error — or worse, leaving the system stuck in a blocked state, idempotency ensures that a retry either has no effect or replaces the previous action.

For example, when triggering a subscription action twice, we can either reuse the last payment intent secret or cancel the incomplete subscription and trigger a new one. The latter variant is more robust as we do not have to address the scenario where the intent secret has potentially expired.

Stripe does not make any guarantees that events are delivered in the correct order. Despite redelivery attempts, messages may be lost, or we might receive unexpected messages after manual interventions, i.e. changes in the Stripe dashboard. As a result, the actor must permit transitions that seem impossible given the local state.

# Actor system
We treat each customer as a separate actor. The actor's system spawns actors on demand and keeps their state in memory.

```rust
class ActorSystem {
  actors      : Map<ActorRef<UserBillingEvent>, UserBillingActor>;
  byUserId    : Map<Str, ActorRef<UserBillingEvent>>;
  byCustomerId: Map<Str, ActorRef<UserBillingEvent>>;

  fn getByUserId(publicId: Str): Actor<UserBillingEvent> { /* ... */ }
  fn getByCustomerId(stripeCustomerId: Str): Actor<UserBillingEvent> { /* ... */ }
}
```

When receiving a webhook, we resolve the customer actor, map the event, and send a message to the actor.

To reduce memory usage, a policy could be applied to evict actors without recent activity. This is safe as Stripe holds the ground truth over billing data, and the actor is implemented so that it fetches the required information on demand.

# Testing
After setting up a Stripe sandbox, the system can be verified by simulating billing scenarios using automated tests. The Stripe sandbox supports test clocks, allowing even recurring payments to be tested. We perform snapshot testing on the actor state to reduce the maintenance effort as the actor state tends to evolve when new scenarios are added.

Each test is self-contained and tests a realistic billing scenario. The following steps show an example of resubscription after cancellation:

- Create user in mock database, start actor
- Change plan to Basic
- Pay open invoice
- Wait until `action` is `Idle`, compare state snapshot
- Cancel subscription
- Wait until `cancelled` is set and true
- Change plan to Premium
- Wait until `action` is `Idle`, compare state snapshot

Since actors abstract the billing domain at a high level, these tests are straightforward to write. By snapshotting the full state, we can avoid regressions in seemingly unrelated fields.

# Deployment
To keep the actor logic simple and to eliminate the need for cross-actor synchronisation, we limit the billing service to a single replica. To have multiple replicas, one could configure the ingress server to route payment endpoints and webhook events to the same instance based on the user ID. Using a message bus for synchronising actor state changes would be an option if maximum availability is required, at the cost of higher complexity.

# Conclusion
By modelling the billing flow using actors, we found our integration to be more robust and easier to maintain compared with using the Stripe library directly. These benefits arise from the explicit modelling of billing state, domain actions and events, and maintaining separate actors for each customer.

Key design principles include abstracting billing actions into commands, operating only on domain events, keeping the actor state minimal, preferring references to external entities, incrementally populating the actor state, keeping actions idempotent, and verifying scenarios through snapshot testing.
