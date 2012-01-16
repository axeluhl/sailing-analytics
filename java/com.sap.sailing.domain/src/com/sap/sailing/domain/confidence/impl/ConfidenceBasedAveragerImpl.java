package com.sap.sailing.domain.confidence.impl;

import java.util.Iterator;

import com.sap.sailing.domain.common.Util;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.ScalableValue;


/**
 * A set of values that {@link HasConfidence have a confidence attached} can be averaged using the confidence value to
 * weigh values with greater confidence more than those with lesser confidence. It implements the following algorithm
 * for a sequence of <code>n</code> tuples <code>(v1, c1), ..., (vn, cn)</code> of a value <code>vi</code> with a
 * confidence <code>ci</code> each:<p>
 * 
 * <code>a := (c1*v1 + ... + cn*vn) / (c1 + ... + cn)</code>. For a single value with a confidence this trivially
 * results in <code>c1*v1 / (c1)</code> which is equivalent to <code>v1</code>. As another example, consider two values
 * with equal confidence <code>0.8</code>. Then, <code>a := (0.8*v1 + 0.8*vn) / (0.8 + 0.8)</code> which resolves to
 * <code>0.5*v1 + 0.5*v2</code> which is obviously the arithmetic mean of the two values. If one value has confidence
 * <code>0.8</code> and the other <code>0.4</code>, then <code>a := (0.8*v1 + 0.4*vn) / (0.8 + 0.4)</code> which
 * resolves to <code>2/3*v1 + 1/3*v2</code> which is a weighed average.
 * <p>
 * 
 * Note, that this doesn't exactly take facts for facts. In other words, if one value is provided with a confidence of
 * <code>1.0</code>, the average may still be influenced by other values. However, this cleanly resolves otherwise
 * mutually contradictory "facts" such a <code>(v1, 1.0), (v2, 1.0)</code> with <code>v1 != v2</code>. It is considered
 * bad practice to claim a fact as soon as it results from any kind of measurement or estimation. All measurement
 * devices produce some statistical errors, no matter how small (cf. Heisenberg ;-) ).
 * 
 * @author Axel Uhl (d043530)
 */
public class ConfidenceBasedAveragerImpl<ValueType, AveragesTo> implements ConfidenceBasedAverager<ValueType, AveragesTo> {
    @Override
    public AveragesTo getAverage(Iterable<? extends HasConfidence<ValueType, AveragesTo>> values) {
        if (values == null || Util.isEmpty(values)) {
            return null;
        } else {
            Iterator<? extends HasConfidence<ValueType, AveragesTo>> iter = values.iterator();
            HasConfidence<ValueType, AveragesTo> next = iter.next();
            ScalableValue<ValueType, AveragesTo> numerator = next.getScalableValue().multiply(next.getConfidence());
            double confidenceSum = next.getConfidence();
            while (iter.hasNext()) {
                next = iter.next();
                numerator = numerator.add(next.getScalableValue().multiply(next.getConfidence()));
                confidenceSum += next.getConfidence();
            }
            // TODO determine new confidence, e.g., based on the variance of estimates averaged
            double newConfidence = confidenceSum / Util.size(values);
            AveragesTo result = numerator.divide(confidenceSum, newConfidence);
            return result;
        }
    }
}
