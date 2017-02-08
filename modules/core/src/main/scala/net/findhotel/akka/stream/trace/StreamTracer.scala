package net.findhotel.akka.stream.trace;

/**
 * Created by rrh on 22/12/16.
 */

import org.apache.htrace.core.{ HTraceConfiguration, Tracer }

object StreamTracer {
  val tracer: Tracer = new Tracer.Builder("akka-http-tracer").
    conf(new HTraceConfiguration() {

      println("Creatin system config")
      override def get(key: String) = {
        System.getProperty("htrace." + key)
      }

      override def get(key: String, defaultValue: String) = {
        val ret = get(key)
        ret match {
          case s: String => s
          case _ => defaultValue
        }
      }
    }).build()
}
