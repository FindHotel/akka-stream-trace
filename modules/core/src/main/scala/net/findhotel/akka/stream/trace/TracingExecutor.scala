package net.findhotel.zedek.core.trace

import java.util.concurrent.{ ExecutorService, Executors, ThreadFactory }

import akka.dispatch._
import com.typesafe.config.Config
import net.findhotel.akka.stream.trace.StreamTracer
import org.apache.htrace.core.Tracer

/**
 * Created by rrh on 20/12/16.
 */
class TracingExecutorConfigurator(config: Config, prerequisites: DispatcherPrerequisites) extends ExecutorServiceConfigurator(config, prerequisites) {

  def createExecutorServiceFactory(id: String, threadFactory: ThreadFactory): ExecutorServiceFactory = {
    TracingExecutorFactory(id, threadFactory, StreamTracer.tracer)
  }
}

case class TracingExecutorFactory(id: String, tf: ThreadFactory, tracer: Tracer) extends ExecutorServiceFactory {

  override def createExecutorService: ExecutorService = {
    tracer.newTraceExecutorService(Executors.newCachedThreadPool(tf), id)
  }

}

