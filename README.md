
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
