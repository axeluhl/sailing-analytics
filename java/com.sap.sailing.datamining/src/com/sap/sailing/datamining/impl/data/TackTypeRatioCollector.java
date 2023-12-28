package com.sap.sailing.datamining.impl.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.domain.common.TackType;

public abstract class TackTypeRatioCollector<ADDABLE> implements Collector<HasTackTypeSegmentContext, Map<TackType, ADDABLE>, Double> {

    private final ADDABLE nullValue;

    public TackTypeRatioCollector(ADDABLE nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public Supplier<Map<TackType, ADDABLE>> supplier() {
        return ()->{
            final Map<TackType, ADDABLE> result = new HashMap<>();
            for (final TackType tt : TackType.values()) {
                result.put(tt, nullValue);
            }
            return result;
        };
    }

    @Override
    public BiConsumer<Map<TackType, ADDABLE>, HasTackTypeSegmentContext> accumulator() {
        return (sumPerTackType, element)->{
            final TackType tt = element.getTackType();
            synchronized (this) {
                sumPerTackType.put(tt, add(sumPerTackType.get(tt), getAddable(element)));
            }
        };
    }


    @Override
    public BinaryOperator<Map<TackType, ADDABLE>> combiner() {
        return (r1, r2) -> {
            for (final Entry<TackType, ADDABLE> e : r1.entrySet()) {
                r2.put(e.getKey(), add(e.getValue(), r2.get(e.getKey())));
            }
            return r2;
        };
    }

    

    @Override
    public Function<Map<TackType, ADDABLE>, Double> finisher() {
        return sumPerTackType->{
            final ADDABLE shortTackSum = sumPerTackType.get(TackType.SHORTTACK);
            return shortTackSum.equals(nullValue) ? null : divide(sumPerTackType.get(TackType.LONGTACK), shortTackSum);
        };
    }


    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    protected abstract ADDABLE add(ADDABLE a, ADDABLE b);
    
    protected abstract double divide(ADDABLE a, ADDABLE b);
    
    protected abstract ADDABLE getAddable(HasTackTypeSegmentContext element);
}
