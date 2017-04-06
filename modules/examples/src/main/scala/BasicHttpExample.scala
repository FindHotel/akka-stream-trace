import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, Uri }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.stream.scaladsl.Source
import net.findhotel.akka.stream.trace.Directives

import scala.concurrent.{ Await, Future }
import scala.util.Try

/**
 * Created by rrh on 13/03/17.
 */
object BasicHttpExample extends App {
  implicit val asystem = ActorSystem("simple-http-example")
  val set = ActorMaterializerSettings(asystem)
    .withInputBuffer(Math.pow(2, 6).toInt, Math.pow(2, 10).toInt)
    .withAutoFusing(true)
    .withDispatcher("tracing-dispatcher")

  implicit val ec = asystem.dispatcher
  implicit val amaterializer = ActorMaterializer(set)

  val frontend_route =
    path("frontend") {
      get {
        Directives.extractTraceId { t_id =>
          val rand = (Math.random() * 10).toInt
          println(t_id)
          val req = HttpRequest(uri = s"http://localhost.charlesproxy.com:8080/backend?i=$rand").withHeaders(t_id.get, RawHeader(Directives.HTRACE_HEADER_NAME, t_id.get.value), RawHeader("foo", "bar"))
          val res = Http().singleRequest(req)
          complete(res)
        }
      }
    }

  val backend_route = path("backend") {
    get {
      Directives.extractTraceId { t_id =>
        println(t_id)
        parameter('i.as[Int]) { i =>
          val result = Source.fromIterator(() => (1 until i).toIterator).async
            .mapAsyncUnordered(9)(i => Future {
              Thread.sleep(100)
              factorial(i)
            }(ec)).runFoldAsync(Seq[Long]()) {
              (acc, curr) =>
                Future(acc :+ curr)
            }
          complete(result.map(_.mkString(",")))
        }
      }
    }
  }

  def factorial(n: Int): Long = n match {
    case 0 => 1
    case _ => n * factorial(n - 1)
  }

  import scala.concurrent.duration._
  val wrapped_route = Directives.wrap(frontend_route ~ backend_route)
  val bindingFuture = Http().bindAndHandle(wrapped_route, "0.0.0.0", 8080)(amaterializer)
  val bind = Await.result(bindingFuture, 10 seconds)
}

