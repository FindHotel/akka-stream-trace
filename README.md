
[![Gitter Chat](http://img.shields.io/badge/chat-online-brightgreen.svg)](https://gitter.im/akka-stream-trace) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.findhotel/akka-stream-trace_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.findhotel/akka-stream-trace)


Htrace extension for akka http and akka streams
==============
prerequistes: 
```
docker run -d -p 9411:9411 -p 9410:9410 openzipkin/zipkin
```

running the examples:
```
sbt examples/run
```
then choose 1 for http example or 2 for streaming example 

The http example will bind to port 8080. point your browesr to localhost:8080/frontend in order to initiate a request. 


Getting Started: 

The main advantage of using this library is that it eliminates the need for manual calls for logs and lets you focus on the 
right things, when things break you will still have a clear view into your applications, you can always add anontations later. 

add the dependecy: 
```
resolvers +=  Resolver.sonatypeRepo("releases") //akka-stream-trace sources 
resolvers += "Apache OSS Snapshots" at "https://repository.apache.org/content/groups/snapshots/" // htrace sources
libraryDependencies += "net.findhotel" %% "akka-stream-trace" % "0.3"
```

The first part of integrating the library is adding a config file, here's a basic example: 

```
htrace{
  span.receiver.classes = org.apache.htrace.impl.ZipkinSpanReceiver
  sampler.classes = org.apache.htrace.core.AlwaysSampler
  zipkin.scribe.hostname = "localhost"
}
```
This example tells htrace to use the zipkin receiver, samples all traces and points htrace to `localhost` as the span server.

now we have to configure the actor system to use the tracing dispatcher 

```
  implicit val as = ActorSystem()
  val set = ActorMaterializerSettings(as)
  //We want to use the tracing dispatcher
  .withDispatcher("tracing-dispatcher")
  implicit val met = ActorMaterializer(set)
```



if you are using `akka-http` you are done, if this is a akka-streams applications add this call before your call to `run`:

```
val s = StreamTracer.tracer.newScope("compute")
Source.fromIterator(() => (1 to 100).toIterator).runFold(0)(_ +_)
```
don't forget to close the scope after the call to run

```
s.close()
```

this method is shown in here https://github.com/FindHotel/akka-stream-trace/blob/master/modules/examples/src/main/scala/BasicStreamExample.scala

Videos: 

Tracing akka streams (scalar 2017):

[![Tracing Akka Steams with htrach](http://i.imgur.com/SpbeaUM.png)](http://www.youtube.com/watch?v=HxQUJThYvw8 "Tracing Akka Streams")
