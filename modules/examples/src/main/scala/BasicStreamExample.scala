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

  val s = StreamTracer.tracer.newScope("compute")
  val ss = Source.fromIterator(() => (1 until 10).toIterator).async
    .mapAsyncUnordered(4)(i => Future {
      Thread.sleep(100)
      i * i
    }(ec))
  val seq = ss.runFoldAsync(Seq[Int]()) {
    (acc, curr) =>
      Future(acc :+ curr)
  }
  s.close()

  seq.onSuccess {
    case list @ x :: xs =>
      s.addKVAnnotation("list", list.mkString(","))
      println(list)
      system.terminate()
  }

}