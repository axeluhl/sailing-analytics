package com.sap.sse.common.scalablevalue;


/**
 * A {@link ScalableValue} where the <code>ValueType</code> and the <code>AveragesTo</code> type are both the same because
 * the value type is itself scalable, offering addition, multiplication and division.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface AbstractScalarValue<T extends Comparable<T>> extends ComparableScalableValueWithDistance<T, T> {
    
}
