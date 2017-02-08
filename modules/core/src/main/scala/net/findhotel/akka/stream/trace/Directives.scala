package net.findhotel.akka.stream.trace

import akka.NotUsed
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings._
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.apache.htrace.core.{ SpanId, TraceScope, Tracer }

import scala.concurrent.{ Await, ExecutionContextExecutor, Promise }

object Directives {
  val tracer = StreamTracer.tracer
  val HTRACE_HEADER_NAME = "x-htrace-span-id"

  def handleNewScope(s: TraceScope) = {

  }
  def createHtraceHeader(scope: TraceScope) = RawHeader(HTRACE_HEADER_NAME, scope.getSpanId.toString)
  def createScope(c: RequestContext): PartialFunction[Option[String], TraceScope] = {
    case Some(span_id) => {
      val s = tracer.newScope(c.request.uri.path.toString(), SpanId.fromString(span_id))
      s.addKVAnnotation("path", c.unmatchedPath.toString())
      s
    }
    case None => {
      val s = tracer.newScope(c.request.uri.path.toString())
      s.addKVAnnotation("path", c.unmatchedPath.toString())
      s
    }
  }

  val findScope = for {
    c <- extractRequestContext
    q <- optionalHeaderValueByName(HTRACE_HEADER_NAME)
  } yield createScope(c).apply(q)

  val startScope = findScope.flatMap(scope => mapRequestContext(_.mapRequest(v => v.copy(headers = v.headers :+ createHtraceHeader(scope)))).tmap(v => scope))
    .flatMap(scope => respondWithDefaultHeader(createHtraceHeader(scope))
      .tmap(v => scope))

  def wrap(route: Route)(implicit
    routingSettings: RoutingSettings,
    parserSettings: ParserSettings,
    materializer: Materializer,
    routingLog: RoutingLog,
    executionContext: ExecutionContextExecutor = null,
    rejectionHandler: RejectionHandler = RejectionHandler.default,
    exceptionHandler: ExceptionHandler = null): Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest].mapAsync(1) { req =>
    Route.asyncHandler(Directives.startScope { s => s.addTimelineAnnotation("inside route"); route }).apply(req).map { res =>
      val traceID = res.getHeader("x-htrace-span-id").get.value()
      val s = StreamTracer.tracer.newScope("ffoo", SpanId.fromString(traceID))
      s.addTimelineAnnotation("closing")
      s.reattach()
      res
    }
  }
}
