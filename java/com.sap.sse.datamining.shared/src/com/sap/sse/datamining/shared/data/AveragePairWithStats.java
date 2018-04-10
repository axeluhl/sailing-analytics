package com.sap.sse.datamining.shared.data;

import java.io.Serializable;

import com.sap.sse.common.Util.Pair;

/**
 * Result of an averaging operation, augmented by statistical data explaining a bit more about how the average was
 * computed, including minimum and maximum values, count, median, as well as standard deviation. All but count will have
 * the same type of the average value whereas the count is an integer-like data type.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public interface AveragePairWithStats<T> extends Serializable {
    Pair<T, T> getAverage();
    Pair<T, T> getMin();
    Pair<T, T> getMax();
    Pair<T, T> getMedian();
    Pair<T, T> getStandardDeviation();
    long getCount();
    
    /**
     * @return the fully-qualified type name of the {@code T} type argument; may be used by
     * the results presenter to choose an applicable data provider.
     */
    String getResultType();
}
