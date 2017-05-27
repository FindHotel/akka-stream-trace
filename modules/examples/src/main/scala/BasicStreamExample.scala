package net.findhotel.akka.stream.trace.examples

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.stream.scaladsl.{ Sink, Source }
import net.findhotel.akka.stream.trace.StreamTracer

import scala.concurrent.Future

/**
 * Created by rrh on 13/03/17.
 */
object BasicStreamExample extends App {
  implicit val system = ActorSystem("basic-tracing-example")
  val set = ActorMaterializerSettings(system)
    //We want to use the tracing dispatcher
    .withDispatcher("tracing-dispatcher")
  implicit val ec = system.dispatcher
  implicit val amaterializer = ActorMaterializer(set)

  //We need to manually create a scope here since this computation stands on it's own

  val s = StreamTracer.tracer.newScope("compute")
  val ss = Source.fromIterator(() => (1 until 10).toIterator)
    .mapAsyncUnordered(4)(i => Future {

      i * i
    }(ec))
  val seq = ss.runFoldAsync(Seq[Int]()) {
    (acc, curr) =>
      Future(acc :+ curr)
  }

  seq.onSuccess {
    case list @ x :: xs =>
      s.addKVAnnotation("list", list.mkString(","))
      println(list)
      system.terminate()
  }
  s.close()
}