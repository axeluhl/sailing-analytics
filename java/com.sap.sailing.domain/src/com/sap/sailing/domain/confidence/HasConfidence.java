package com.sap.sailing.domain.confidence;

/**
 * Some values, particularly those obtained from real-world measurements, are not always accurate. Some
 * values are derived by interpolating or extrapolating data series obtained through measurement or
 * even estimation. Some values are simply guessed by humans and entered into the system.<p>
 * 
 * All those values have a certain level of confidence. In case multiple sources of information about the
 * same entity or phenomenon are available, knowing the confidence of each value helps in weighing and
 * averaging these values more properly than would be possible without a confidence value.
 * 
 * @author Axel Uhl (d043530)
 * 
 * @param <ValueType> the type of the scalable value. See particularly the {@link #scale()} method.
 *
 */
public interface HasConfidence<ValueType, AveragesTo> {
    /**
     * A confidence is a number between 0.0 and 1.0 (inclusive) where 0.0 means that the value is randomly guessed while
     * 1.0 means the value is authoritatively known for a fact. It represents the weight with which a value is to be
     * considered by averaging, interpolation and extrapolation algorithms.
     * <p>
     * 
     * An averaging algorithm for a sequence of <code>n</code> tuples <code>(v1, c1), ..., (vn, cn)</code> of a value
     * <code>vi</code> with a confidence <code>ci</code> each can for example look like this:
     * <code>a := (c1*v1 + ... + cn*vn) / (c1 + ... + cn)</code>. For a single value with a confidence this trivially
     * results in <code>c1*v1 / (c1)</code> which is equivalent to <code>v1</code>. As another example, consider two
     * values with equal confidence <code>0.8</code>. Then, <code>a := (0.8*v1 + 0.8*vn) / (0.8 + 0.8)</code> which
     * resolves to <code>0.5*v1 + 0.5*v2</code> which is obviously the arithmetic mean of the two values. If one value
     * has confidence <code>0.8</code> and the other <code>0.4</code>, then
     * <code>a := (0.8*v1 + 0.4*vn) / (0.8 + 0.4)</code> which resolves to <code>2/3*v1 + 1/3*v2</code> which is a
     * weighed average.
     * <p>
     * 
     * Note, that this doesn't exactly take facts for facts. In other words, if one value is provided with a confidence
     * of <code>1.0</code>, the average may still be influenced by other values. However, this cleanly resolves otherwise
     * mutually contradictory "facts" such a <code>(v1, 1.0), (v2, 1.0)</code> with <code>v1 != v2</code>. It is
     * considered bad practice to claim a fact as soon as it results from any kind of measurement or estimation. All
     * measurement devices produce some statistical errors, no matter how small (cf. Heisenberg ;-) ).
     */
    double getConfidence();
    
    ScalableValue<ValueType, AveragesTo> getScalableValue();
    
}
