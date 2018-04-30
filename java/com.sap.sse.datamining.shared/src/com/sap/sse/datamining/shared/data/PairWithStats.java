package com.sap.sse.datamining.shared.data;

import java.io.Serializable;
import java.util.HashSet;

import com.sap.sse.common.Util.Pair;

public interface PairWithStats<T> extends Serializable {
    Pair<T, T> getAverage();
    Pair<T, T> getMin();
    Pair<T, T> getMax();
    Pair<T, T> getMedian();
    Pair<T, T> getStandardDeviation();
    HashSet<Pair<T,T>> getIndividualPairs();
    long getCount();
    
    /**
     * @return the fully-qualified type name of the {@code T} type argument; may be used by
     * the results presenter to choose an applicable data provider.
     */
    String getResultType();
}
