Set of examples which demonstrate basic akka-stream-tracing usage.

*Prerequisites*:
scala to run the exmplaes, docker to run zipkin


*Start zipkin server*:

```
docker run -d -p 9411:9411 -p 9410:9410 openzipkin/zipkin
```

*Run examples*:

from command line:
`sbt run examples/[Example classname]`
like:
`sbt run examples/BasicStreamExample`