package net.findhotel.akka.stream.trace

import com.typesafe.config.ConfigFactory

import scala.util.Try
/**
 * Created by rrh on 22/12/16.
 */

import org.apache.htrace.core.{ HTraceConfiguration, Tracer }

object StreamTracer {
  val conf = ConfigFactory.load()
  val tracer: Tracer = new Tracer.Builder("akka-http-tracer").
    conf(new HTraceConfiguration() {
      override def get(key: String) = {
        val c_key = "htrace." + key
        val value = Try(conf.getString(c_key)).getOrElse(null)
        println(s"$c_key", value)
        value
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
