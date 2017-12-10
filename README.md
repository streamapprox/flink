Flink-based StreamApprox
========================

This prototype implements the online adaptive stratified reservoir sampling ([OASRS](https://dl.acm.org/citation.cfm?id=3135989&CFID=1011170257&CFTOKEN=36003206)) algorithm using Apache Flink 1.2.0

### Build

Using Maven to build Flink-based StreamApprox.
See [scripts](https://github.com/streamapprox/flink-setup) for Spark/Flink building and installation instructions.

### Usage

This prototype implements OASRS sampling algorithm to support  a sampling function for Apache Flink.
Users can use this function as an operator (UDF) of Flink.


```java
    DataStream<Tuple2<String, Double>>  sampleStream =
         dataStream.transform("OASRS Sampling", TypeInformation.of(new TypeHint<Tuple3<String, Double, Double>>() {}),
         new Sampler<Tuple2<String, Double>, Tuple3<String, Double, Double>>(oasrs))
         .flatMap(new WeightSum())
         .keyBy(0)
         .window(SlidingProcessingTimeWindows.of(seconds(10), seconds(5)))
         .sum(1);
```

### Support
* If you have any question please shoot me an email: do.le_quoc@tu-dresden.de

### License
Published under GNU General Public License v2.0 (GPLv2)
