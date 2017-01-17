package articles

import pl.metastack.metadocs.SectionSupport

import scala.concurrent.Future

object DependentTypes extends SectionSupport {
  section("ownership") {
    trait Client {
      type Connection

      def connect: Connection
      def disconnect(connection: Connection): Unit
    }

    class ClientOps extends Client {
      override type Connection = Int

      override def connect: Connection = 42
      override def disconnect(connection: Connection): Unit = {}
    }

    object Client {
      def apply(): Client = new ClientOps
    }
  }

  section("type-mapping") {
    sealed trait Request { type R <: Response }
    sealed trait Response

    object Response {
      case class LogIn(hash: Option[String])
        extends Response
      case class List(users: Seq[String], pages: Int)
        extends Response
    }

    object Request {
      case class LogIn(username: String, password: String)
        extends Request { override type R = Response.LogIn }
      case class List(page: Int)
        extends Request { override type R = Response.List  }
    }

    sealed trait Service[R <: Request] {
      def apply(req: R): Future[req.R]
    }

    implicit object LogInSvc extends Service[Request.LogIn] {
      override def apply(req: Request.LogIn): Future[req.R] =
        Future.successful(Response.LogIn(None))
    }

    implicit object ListSvc extends Service[Request.List] {
      override def apply(req: Request.List): Future[req.R] =
        Future.successful(Response.List(List.empty, 1))
    }

    def request[R <: Request](req: R)(implicit svc: Service[R]) =
      svc.apply(req)
  }
}
