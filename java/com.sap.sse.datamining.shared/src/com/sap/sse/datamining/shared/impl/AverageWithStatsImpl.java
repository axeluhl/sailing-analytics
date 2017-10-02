package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.data.AverageWithStats;

public class AverageWithStatsImpl<T> implements AverageWithStats<T> {
    private static final long serialVersionUID = -3094183173270210846L;
    private final T average;
    private final T min;
    private final T max;
    private final T median;
    private final T standardDeviation;
    private final long count;
    private final String resultType;

    public AverageWithStatsImpl(T average, T min, T max, T median, T standardDeviation, long count, Class<T> resultType) {
        super();
        this.average = average;
        this.min = min;
        this.max = max;
        this.median = median;
        this.standardDeviation = standardDeviation;
        this.count = count;
        this.resultType = resultType.getName();
    }

    @Override
    public T getAverage() {
        return average;
    }

    @Override
    public T getMin() {
        return min;
    }

    @Override
    public T getMax() {
        return max;
    }

    @Override
    public T getMedian() {
        return median;
    }

    @Override
    public T getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public String getResultType() {
        return resultType;
    }
}
