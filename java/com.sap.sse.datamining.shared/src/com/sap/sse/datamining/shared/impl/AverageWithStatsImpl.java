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

    public AverageWithStatsImpl(T average, T min, T max, T median, T standardDeviation, long count, String resultType) {
        super();
        this.average = average;
        this.min = min;
        this.max = max;
        this.median = median;
        this.standardDeviation = standardDeviation;
        this.count = count;
        this.resultType = resultType;
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

    @Override
    public String toString() {
        return "AverageWithStatsImpl [average=" + average + ", min=" + min + ", max=" + max + ", median=" + median
                + ", standardDeviation=" + standardDeviation + ", count=" + count + ", resultType=" + resultType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((average == null) ? 0 : average.hashCode());
        result = prime * result + (int) (count ^ (count >>> 32));
        result = prime * result + ((max == null) ? 0 : max.hashCode());
        result = prime * result + ((median == null) ? 0 : median.hashCode());
        result = prime * result + ((min == null) ? 0 : min.hashCode());
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + ((standardDeviation == null) ? 0 : standardDeviation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AverageWithStatsImpl<?> other = (AverageWithStatsImpl<?>) obj;
        if (average == null) {
            if (other.average != null)
                return false;
        } else if (!average.equals(other.average))
            return false;
        if (count != other.count)
            return false;
        if (max == null) {
            if (other.max != null)
                return false;
        } else if (!max.equals(other.max))
            return false;
        if (median == null) {
            if (other.median != null)
                return false;
        } else if (!median.equals(other.median))
            return false;
        if (min == null) {
            if (other.min != null)
                return false;
        } else if (!min.equals(other.min))
            return false;
        if (resultType == null) {
            if (other.resultType != null)
                return false;
        } else if (!resultType.equals(other.resultType))
            return false;
        if (standardDeviation == null) {
            if (other.standardDeviation != null)
                return false;
        } else if (!standardDeviation.equals(other.standardDeviation))
            return false;
        return true;
    }
}
