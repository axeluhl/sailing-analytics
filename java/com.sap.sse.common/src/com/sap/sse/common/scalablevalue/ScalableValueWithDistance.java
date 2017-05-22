package com.sap.sse.common.scalablevalue;

/**
 * In addition to being a {@link ScalableValue}, specifies the {@link #getDistance} method which determines
 * a non-negative <code>double</code> value as a measure for the distances between two values of this type.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <ValueType>
 * @param <AveragesTo>
 */
public interface ScalableValueWithDistance<ValueType, AveragesTo> extends ScalableValue<ValueType, AveragesTo> {
    /**
     * @return a non-negative distance measure; <code>0.0</code> in case <code>this</code> and <code>other</code> are
     *         equal.
     */
    double getDistance(AveragesTo other);
    
    ScalableValueWithDistance<ValueType, AveragesTo> add(ScalableValue<ValueType, AveragesTo> t);

    ScalableValueWithDistance<ValueType, AveragesTo> multiply(double factor);
}
