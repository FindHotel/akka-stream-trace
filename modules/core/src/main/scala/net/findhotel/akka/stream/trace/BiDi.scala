package net.findhotel.zedek.core.trace

import akka.NotUsed
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.stream._
import akka.stream.scaladsl.{ BidiFlow, Flow }
import akka.stream.stage._

/**
 * Wraps an {@code Flow[HttpRequest,HttpResponse]} with the necessary steps to output
 * the http metrics defined in AkkaHttpServerMetrics.
 * credits to @jypma.
 */
object FlowWrapper {

  def wrap() = new GraphStage[BidiShape[HttpRequest, HttpRequest, HttpResponse, HttpResponse]] {

    val requestIn = Inlet.create[HttpRequest]("request.in")
    val requestOut = Outlet.create[HttpRequest]("request.out")
    val responseIn = Inlet.create[HttpResponse]("response.in")
    val responseOut = Outlet.create[HttpResponse]("response.out")

    override val shape = BidiShape(requestIn, requestOut, responseIn, responseOut)

    override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) {

      setHandler(requestIn, new InHandler {
        override def onPush(): Unit = {
          val request = grab(requestIn)

          println("in create logic", request)
          push(requestOut, request)
        }
      })

      setHandler(requestOut, new OutHandler {
        override def onPull(): Unit = pull(requestIn)
      })

      setHandler(responseIn, new InHandler {
        override def onPush(): Unit = {
          push(responseOut, grab(responseIn))
        }
      })

      setHandler(responseOut, new OutHandler {
        override def onPull(): Unit = pull(responseIn)
      })

      //      override def preStart(): Unit = metrics.recordConnectionOpened()
      //      override def postStop(): Unit = metrics.recordConnectionClosed()
    }
  }

  def apply(flow: Flow[HttpRequest, HttpResponse, NotUsed]) = BidiFlow.fromGraph(wrap())

  private def includeTraceToken(response: HttpResponse, traceTokenHeaderName: String, token: String): HttpResponse = response match {
    case response: HttpResponse ⇒ response.withHeaders(response.headers ++ Seq(RawHeader(traceTokenHeaderName, token)))
    case other ⇒ other
  }
}