package se.tud.streamapprox.sampling;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.summarize.aggregation.IntegerSummaryAggregator;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import static org.apache.flink.streaming.api.windowing.time.Time.seconds;


/**
 * Created by lequocdo on 13/04/17.
 */
public class TestSampling {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        int sampleSize = 10;

        StratifiedReservoirSampling oasrs = new StratifiedReservoirSampling(sampleSize);
        DataStream<Tuple2<String, Integer>> dataStream = env
                .socketTextStream("localhost", 9999)
                .flatMap(new Splitter());
        SingleOutputStreamOperator<Tuple2<String, Double>> sampleStream =
                dataStream.transform("OASRS Sampling", TypeInformation.of(new TypeHint<Tuple3<String, Integer, Double>>() {}),
                new Sampler<Tuple2<String, Integer>, Tuple3<String, Integer, Double>>(oasrs))
                        .flatMap(new WeightSum())
                        .keyBy(0)
                        .window(SlidingProcessingTimeWindows.of(seconds(4), seconds(2)))
                        .sum(1);

        sampleStream.print();
        System.err.println(env.getExecutionPlan());
        env.execute("Window WordCount");
    }

    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word : sentence.split(" ")) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
        }
    }

    public static class WeightSum implements FlatMapFunction<Tuple3<String, Integer, Double>, Tuple2<String, Double>> {
        @Override
        public void flatMap(Tuple3<String, Integer, Double> item, Collector<Tuple2<String, Double>> out) throws Exception {
            double value = item.f1 * item.f2;
            out.collect(new Tuple2<String, Double>(item.f0, value));
        }
    }

}
