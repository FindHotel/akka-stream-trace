package net.findhotel.akka.stream.trace

import akka.NotUsed
import akka.http.scaladsl.model.headers.{ ModeledCustomHeader, ModeledCustomHeaderCompanion }
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings._
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import org.apache.htrace.core.{ SpanId, TraceScope }

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

object Directives {
  val tracer = StreamTracer.tracer

  val HTRACE_HEADER_NAME = Try(ConfigFactory.load().getString("htrace-header-name")).getOrElse("x-htrace-span-id")

  def handleNewScope(r: HttpRequest, span_id: Option[String]) = {
    val scope = span_id match {
      case Some(id) => tracer.newScope(r.uri.path.toString(), SpanId.fromString(id))
      case None => tracer.newScope(r.uri.path.toString())
    }
    r.uri.rawQueryString match {
      case Some(qs) => scope.addKVAnnotation("query string", qs)
      case None => scope.addKVAnnotation("query string", "no query string")
    }

    scope
  }
  def createHtraceHeader(ts: TraceScope) = HtraceHeader(ts.getSpanId.toString)

  val findScope = optionalHeaderValueByName(HTRACE_HEADER_NAME)

  val startScope = (scope: TraceScope) => {
    scope.detach()
    findScope.map { v => scope.reattach(); v }.flatMap(_ => respondWithDefaultHeader(createHtraceHeader(scope)))
  }

  val extractTraceId = findScope.map(maybeHeader => maybeHeader.map(HtraceHeader.apply))

  def wrap(route: Route)(implicit
    routingSettings: RoutingSettings,
    parserSettings: ParserSettings,
    materializer: Materializer,
    routingLog: RoutingLog,
    executionContext: ExecutionContextExecutor = null,
    rejectionHandler: RejectionHandler = RejectionHandler.default,
    exceptionHandler: ExceptionHandler = null): Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest].mapAsync(1) { req =>

    val maybeTraceId = Try(req.getHeader(HTRACE_HEADER_NAME).get().value()).toOption
    val s = handleNewScope(req, maybeTraceId)
    val f = Route.asyncHandler(startScope(s) { route }).apply(req.withHeaders(createHtraceHeader(s)))
    f.andThen { case _ => s.addTimelineAnnotation("Requset closed"); s.reattach(); s.close() }
    s.detach()
    f
  }

  final class HtraceHeader(token: String) extends ModeledCustomHeader[HtraceHeader] {
    override def renderInRequests = true
    override def renderInResponses = true
    override val companion = HtraceHeader
    override def value: String = token
  }

  object HtraceHeader extends ModeledCustomHeaderCompanion[HtraceHeader] {
    override val name = HTRACE_HEADER_NAME
    override def parse(value: String) = Try(new HtraceHeader(value))
  }
}
