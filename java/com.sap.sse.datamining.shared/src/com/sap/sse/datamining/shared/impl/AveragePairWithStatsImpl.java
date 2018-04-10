package com.sap.sse.datamining.shared.impl;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.data.AveragePairWithStats;

public class AveragePairWithStatsImpl<T> implements AveragePairWithStats<T> {
    private static final long serialVersionUID = -3094183173270210846L;
    private final Pair<T, T> average;
    private final Pair<T, T> min;
    private final Pair<T, T> max;
    private final Pair<T, T> median;
    private final Pair<T, T> standardDeviation;
    private final long count;
    private final String resultType;

    public AveragePairWithStatsImpl(Pair<T, T> average, Pair<T, T> min, Pair<T, T> max, Pair<T, T> median, Pair<T, T> standardDeviation, long count, String resultType) {
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
    public Pair<T, T> getAverage() {
        return average;
    }

    @Override
    public Pair<T, T> getMin() {
        return min;
    }

    @Override
    public Pair<T, T> getMax() {
        return max;
    }

    @Override
    public Pair<T, T> getMedian() {
        return median;
    }

    @Override
    public Pair<T, T> getStandardDeviation() {
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
        AveragePairWithStatsImpl<?> other = (AveragePairWithStatsImpl<?>) obj;
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
