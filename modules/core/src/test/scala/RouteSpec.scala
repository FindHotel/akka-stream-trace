/**
  * Created by raam on 1/5/16.
  */
package net.findhotel.akka.stream.trace

import java.util.Base64

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit._
import net.findhotel.akka.stream.trace.Directives.startScope
import org.apache.htrace.core.SpanId
import org.scalatest.{FlatSpec, Matchers}


class RouteSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  val decoder = Base64.getDecoder()
  val r = path("foo"){get{complete("foo")}}

  "simple route" should "should return child span if a parent header id was sent" in {
    val sent_span_id = SpanId.fromString("badae0bcab0149656b3ff944ace9f157")
    val scope = StreamTracer.tracer.newScope("test", sent_span_id)
    val nheader = Directives.createHtraceHeader(StreamTracer.tracer.newScope("test", sent_span_id))
    Get("/foo")  ~> nheader ~> startScope(scope){ r } ~> check {
      val trace_id = header(Directives.HTRACE_HEADER_NAME)
      trace_id.isDefined shouldBe true  // Will fail if -Dhtrace.sampler.classes is not defined.
      val recieved_span_id = SpanId.fromString(trace_id.get.value())
      val curr_span = StreamTracer.tracer.newScope("foo", recieved_span_id)
      recieved_span_id.getHigh shouldBe sent_span_id.getHigh
      status === StatusCodes.OK
    }
  }

  "simple route" should "should return htrace header" in {
    Get("/foo") ~> startScope(StreamTracer.tracer.newNullScope()){ r } ~> check {
      val trace_id = header(Directives.HTRACE_HEADER_NAME)
      trace_id.isDefined shouldBe true  // Will fail if -Dhtrace.sampler.classes is not defined.
      decoder.decode(trace_id.get.value())
      status === StatusCodes.OK
    }
  }
}