package se.tud.streamapprox.sampling;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.XORShiftRandom;

import java.util.*;

/**
 * Created by lequocdo on 14/04/17.
 */
public class StratifiedReservoirSampling<K, V> implements SamplingFunction<Tuple2<K, V>, Tuple3<K, V, Double>> {
    private final Random random;
    public int sampleSize = 0;
    private Map<K, Tuple2<ArrayList<Tuple2<K, V>>, Double>> reservoirs = new HashMap<K, Tuple2<ArrayList<Tuple2<K, V>>, Double>>();
    private Map<K, Integer> count = new HashMap<K, Integer>();

    public StratifiedReservoirSampling(int sampleSize, Random random) {
        this.sampleSize = sampleSize;
        Preconditions.checkArgument(sampleSize >= 0, "sampleSize should be non-negative.");
        this.random = random;
    }

    @Override
    public int getSampleSize() {
        return this.sampleSize;
    }

    public StratifiedReservoirSampling(int sampleSize) {
        this(sampleSize, new XORShiftRandom());
    }

    public StratifiedReservoirSampling(int sampleSize, long seed) {
        this(sampleSize, new XORShiftRandom(seed));
    }

    public void stratifiedReservoirSampling(Tuple2<K, V> item, int sampleSize) {
        double weight = 0;
        K key = item.f0;
        if (!this.reservoirs.containsKey(key)) {
            this.reservoirs.put(key, new Tuple2<ArrayList<Tuple2<K, V>>, Double>(new ArrayList<Tuple2<K, V>>(sampleSize), weight));
            this.count.put(key, 0);
        }
        if (this.count.get(key) < sampleSize) {
            // Fill the queue with first K elements from input.
            weight = 1;
            this.reservoirs.get(key).f0.add(item);
            this.count.put(key, this.count.get(key) + 1);
        } else {
            //if input size > sampleSize, replacing item in reservoir
            weight = this.count.get(key) / sampleSize;
            int replacementIndex = (int) random.nextDouble() * this.count.get(key);
            if (replacementIndex < sampleSize) {
                this.reservoirs.get(key).f0.set(replacementIndex, item);
            }
            this.count.put(key, this.count.get(key) + 1);
        }
        //Update the weight for each item.
        this.reservoirs.get(key).f1 = weight;
    }

    @Override
    public void sample(Tuple2<K, V> item) {
        stratifiedReservoirSampling(item, sampleSize);
    }

    @Override
    public Iterator<Tuple3<K, V, Double>> getItems() {
        List<Tuple3<K, V, Double>> result = new ArrayList<Tuple3<K, V, Double>>();

        for (Map.Entry<K, Tuple2<ArrayList<Tuple2<K, V>>, Double>> element : this.reservoirs.entrySet()) {
            try {
                ArrayList<Tuple2<K, V>> values = element.getValue().f0;
                Double weight = element.getValue().f1;
                for (Tuple2<K, V> item : values) {
                    Tuple3<K, V, Double> out_item = Tuple3.of(item.f0, item.f1, weight);
                    result.add(out_item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result.iterator();
    }

}
