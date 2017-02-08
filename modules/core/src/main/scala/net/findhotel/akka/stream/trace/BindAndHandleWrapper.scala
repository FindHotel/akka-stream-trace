//package net.findhotel.zedek.core.trace
//
//import java.net.InetSocketAddress
//
//import akka.NotUsed
//import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
//import akka.stream._
//import akka.stream.scaladsl.{BidiFlow, Flow, Tcp}
//import akka.stream.stage.{GraphStage, GraphStageLogic}
//import akka.util.ByteString
//
///**
//  * Created by rrh on 06/01/17.
//  */
//class BindAndHandleWrapper(implicit am: ActorMaterializer) {
//
//  implicit val sys = am.system
//  implicit val ec = sys.dispatcher
//  def tracer =  new Graph[BidiShape[HttpRequest, HttpRequest, HttpResponse, HttpResponse], String] {
//    val requestIn = Inlet.create[HttpRequest]("request.in")
//    val requestOut = Outlet.create[HttpRequest]("request.out")
//    val responseIn = Inlet.create[HttpResponse]("response.in")
//    val responseOut = Outlet.create[HttpResponse]("response.out")
//
//    override val shape = BidiShape(requestIn, requestOut, responseIn, responseOut)
//
//
//    override def withAttributes(attr: Attributes): Graph[BidiShape[HttpRequest, HttpRequest, HttpResponse, HttpResponse], String] = ???
//  }
////  def logging: BidiFlow[HttpRequest, HttpResponse, HttpRequest, HttpResponse, NotUsed] = {
////    // function that takes a string, prints it with some fixed prefix in front and returns the string again
////    def logger(prefix: String) = (chunk: ByteString) => {
////      println(prefix + chunk.utf8String)
////      chunk
////    }
////
////    val inputLogger = logger("in > ")
////    val outputLogger = logger("out < ")
////
////    // create BidiFlow with a separate logger function for each of both streams
////    BidiFlow.fromFunctions(inputLogger, outputLogger)
////  }
//
//
//  def adding: BidiFlow[ByteString, ByteString, ByteString, ByteString, NotUsed] = {
//    // function that takes a string, prints it with some fixed prefix in front and returns the string again
//    val in = Flow[ByteString].filter(_.utf8String.contains("h")).map(v =>  ByteString(s"${v.utf8String}!"))
//
//    val out = Flow[ByteString].map{v =>
//      println("###going out ->",v.utf8String)
//      ByteString(s"${v.utf8String}?")}
//
//    // create BidiFlow with a separate logger function for each of both streams
//    BidiFlow.fromFlows(in, out)
//  }
//
//  val f = Flow[ByteString].map(v =>  v ++ ByteString("!"))
//
//  val v =  Tcp().outgoingConnection(new InetSocketAddress("localhost", 5000))
//    //    .via(Framing.delimiter(ByteString("\r\n"), 65536))
//    .map(_.utf8String.replace("\n", "")).map(ByteString(_))
////    .join(logging)
//    .join(adding)
//    .join(Flow[ByteString])
//
//
//
//
//  val res = v.run()
//  res.onComplete(v => println(v.get.remoteAddress))
//  res.recover({
//    case e: Throwable => println(e)
//  })
//
//}
