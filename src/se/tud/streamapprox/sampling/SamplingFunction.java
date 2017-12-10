package se.tud.streamapprox.sampling;

import org.apache.flink.api.common.functions.Function;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by lequocdo on 14/04/17.
 */
public interface SamplingFunction<IN, OUT> extends Function, Serializable {
    public int getSampleSize();
    public void sample(IN item);
    public Iterator<OUT> getItems();
}
