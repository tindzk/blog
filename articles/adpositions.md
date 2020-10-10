+++
title       = "Why study adpositions?"
published   = 2020-10-10
language    = "en-GB"
categories  = ["linguistics", "nlp"]
description = "On the importance of studying adpositions in natural language processing"
references  = "adpositions.toml"
+++

# Introduction
Languages typically feature a small set of adpositions such as *on*, *in*, *with* etc. Yet, adpositions figure among the most frequently occurring words in a language. They serve a wide range of functions, relating objects, actions, topics, events, persons, entities or quantities. Additionally, adpositions are used to express causal, spatial or temporal relationships. With such an exhaustive spectrum of functions, adpositions capture fine-grained semantic information.

Surprisingly, adpositions are often overlooked in NLP. Since adpositions are closely related to semantics, linguistic tasks subject to ambiguity would benefit from efforts to model adpositions. At the same time, they are a suitable target for evaluating the quality of semantic representations in language models.

This article will motivate the study of adpositions with two practical examples from linguistics: PP attachment and entity classification.

# Tasks
## PP attachment
Consider the following two sentences:

1. He bought a shirt with sleeves.
2. He cleaned a shirt with soap.

In the first sentence, the preposition *with* has a comitative meaning (*accompanied by*), whereas in the second sentence the meaning is instrumental (*by means of*). Although both sentences contain the same preposition, their corresponding constituency-based parse trees are different.

The intended meaning is given by these trees:
1. He [bought [[a shirt]<tag>NP</tag> [with sleeves]<tag>PP</tag>]<tag>NP</tag>]<tag>VP</tag>.
2. He [cleaned [a shirt]<tag>NP</tag> [with soap]<tag>PP</tag>]<tag>VP</tag>.

While the following two trees would be valid, they express different meanings which were likely not intended by the speaker:
1. He [bought [a shirt]<tag>NP</tag> [with sleeves]<tag>PP</tag>]<tag>VP</tag>.
2. He [cleaned [[a shirt]<tag>NP</tag> [with soap]<tag>PP</tag>]<tag>NP</tag>]<tag>VP</tag>.

The different sentence readings arise due to ambiguous prepositional phrase (PP) attachments. Depending on the intended semantics, the PP will be either attached to the verbal phrase (VP) or nominal phrase (NP).

Further examples of ambiguous PP attachments in English can be found in @liberman2015.

Since there is a finite number of parse trees, we could rank all variants and choose the most probable one. Assuming binary parse trees, there would be $$C_n$$ trees with $$n + 1$$ leaves as given by the Catalan number (@wikipedia2020): $[C_n = \frac{1}{n+1}{2n\choose n}]$ For a sentence with 10 words this amounts to 4,862 possible trees. The number is considerably higher when breaking words up into morphemes or letters as required for inflecting languages. This indicates the necessity for considering structural constraints to reduce the large combinatorial space. Languages have implicit triggers and constraints which reduce ambiguities in human communication. In the example given, the choice of the verb (*bought* vs. *cleaned*) would be a strong indicator for favouring one parse tree over another.

Interestingly, the ambiguity above does not apply to other languages in the same extent. In Russian, for example, the concepts of accompaniment and instrument are expressed differently whereby ambiguous parse trees do not occur. In the first case, the adposition с (Cyrillic letter 's', meaning *with*) would be used. In the second case, no adposition is added while the word *soap* is set into the instrumental case (мыло → мылом):

1. Он купил рубашку с рукавами. \
   *lit.:* he bought shirt with sleeves<tag>accompaniment</tag>
2. Он постирал рубашку мылом. \
   *lit.:* he cleaned shirt soap<tag>instrument</tag>

This reveals an intricate relationship between adpositions, grammar (case morphology or affixes) and semantics. Most strikingly, while languages represent a shared semantic space, the linguistic means by which semantics is expressed varies.

## Entity classification
The second example we are considering is the linking of semantic roles to entities. In linguistics, an entity denotes a functional group of words. Related NLP tasks include named-entity recognition (NER) and semantic role labelling (SRL), which we jointly refer to as *entity classification*. Examples for entities include names, locations and dates.

The adpositions *by* and *in* trigger entities and form distinct adpositional phrases (AP):

1. Apple was founded [by [Steve Jobs]<tag>name</tag>]<tag>AP</tag> [in California<tag>location</tag>]<tag>AP</tag>.

However, the same adpositions can give rise to different entity roles:

2. Apple was founded [in 1976<tag>year</tag>]<tag>AP</tag>.
3. Sue drove [by car<tag>instrument</tag>]<tag>AP</tag>.

The expressed semantics of an AP does not solely depend on the words in the AP, but also the surrounding context. The context constrains the possible meanings. Verbs are a reliable predictor for the intended meaning. For example, *drove* (3) favours an instrumental meaning as opposed to indicating ownership as in *founded* (1).

The ability to correctly identify and classify entities is critical to NLP applications such as search engines and chatbots. In both cases, user inputs have to be mapped onto a set of pre-defined commands and attributes:

I<tag>Subject</tag> would like [to book]<tag>Intent</tag> a room [for two nights]<tag>Duration</tag> [at your hotel]<tag>Locus</tag> [in October 2018]<tag>Time</tag>.

As with PP attachment, the challenge is to narrow down the semantic space subject to contextual constraints. Due to the interplay of verbs and adpositions, an understanding of the roles an adposition can serve will lead to more reliable entity classification.

# Discussion
We have seen two examples where NLP would benefit from a better understanding of adpositions. In PP attachment, we seek the most probable parse tree that represents the intended meaning. Here, encoding language-specific constraints imposed by adpositions would drastically reduce the search space of possible trees. Entity classification can be viewed as a complementary task which assigns semantic roles to nodes in the parse tree.

Recent language models based on Deep Learning such as GPT-3 learn grammar in an unsupervised way without any prior linguistic features. While these models have impressive generative abilities, it is still unclear how well the learned representations capture fine-grained semantic relationships. Since adpositions are intricately linked to semantics, they are a valuable point of departure for exploring existing Deep Learning models. Adpositions may also spur further research into linguistically-motivated cost functions for domain adaptation.

# References
<references/>
