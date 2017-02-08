/**
 * Created by raam on 1/5/16.
 */
package net.findhotel.akka.stream.trace


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import org.apache.htrace.core.{Span, SpanId}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
object main extends App{
  implicit val asystem = ActorSystem("ZedekApi")
  val set = ActorMaterializerSettings(asystem)
    .withInputBuffer(Math.pow(2, 6).toInt, Math.pow(2, 10).toInt)
    .withAutoFusing(true)
    .withDispatcher("tracing-dispatcher")

  implicit val ec = asystem.dispatcher
  implicit val amaterializer = ActorMaterializer(set)



  val entity = """{"place_id":[1,2,3,4,5,6,7,8,9,10],"location_type":[1,2,3,4],"country_code":["il","cn","jp"]}"""

  val r =
    path("get"){
      get{
        extractRequestContext{c =>
          println("got request!!")
//          val traceID = c.request.getHeader("x-htrace-span-id").get.value()
          val s = StreamTracer.tracer.newScope("fo")

          s.addTimelineAnnotation("rr")
          val rand = (Math.random() * 10).toInt
          val q = Source.fromIterator(() => (1 until rand).toIterator).async
            .mapAsyncUnordered(9)(i => Future{Thread.sleep(100);i * i}(ec)).runFoldAsync(0){(acc, curr) => Future{Thread.sleep(55);acc + curr}}
          val a = Await.result(q, 100000 seconds)
          s.close()
          complete(a.toString)
        }
      }
    }

  import scala.concurrent.duration._
  val q = Directives.wrap(r)

//  Await.result(Http().singleRequest(HttpRequest(uri="http://localhost/get")), 10 seconds)
  val bindingFuture = Http().bindAndHandle(r, "0.0.0.0", 8080)(amaterializer)
  val bind = Await.result(bindingFuture, 10 seconds)
}