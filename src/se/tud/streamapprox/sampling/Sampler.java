package se.tud.streamapprox.sampling;

import org.apache.flink.streaming.api.operators.AbstractUdfStreamOperator;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;

import java.util.Iterator;

/**
 * Created by lequocdo on 14/04/17.
 */
public class Sampler<IN, OUT> extends AbstractUdfStreamOperator<OUT, SamplingFunction<IN, OUT>>
        implements OneInputStreamOperator<IN, OUT> {

    final SamplingFunction<IN, OUT> sampler;
    final int sampleSize;

    public Sampler(SamplingFunction<IN, OUT> UDF) {
        super(UDF);
        this.sampler = UDF;
        this.sampleSize = sampler.getSampleSize();
    }


    @Override
    @SuppressWarnings("unchecked")
    public final void open() throws Exception {
        super.open();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processElement(StreamRecord<IN> item) throws Exception {
        sampler.sample(item.getValue());
        Iterator<OUT> result = sampler.getItems();
        while (result.hasNext()){
            output.collect(new StreamRecord(result.next()));
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

}
