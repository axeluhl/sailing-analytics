package com.sap.sailing.domain.common.confidence.impl;

import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.HasConfidence;
import com.sap.sailing.domain.common.confidence.HasConfidenceAndIsScalable;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sse.common.Util;
import com.sap.sse.common.scalablevalue.ScalableValue;


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
public class ConfidenceBasedAveragerImpl<ValueType, BaseType, RelativeTo> implements ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> {
    private final Weigher<RelativeTo> weigher;
    
    /**
     * @param weigher
     *            If <code>null</code>, 1.0 will be assumed as default confidence for all values provided, regardless
     *            the reference point relative to which the average is to be computed
     */
    public ConfidenceBasedAveragerImpl(Weigher<RelativeTo> weigher) {
        this.weigher = weigher;
    }

    @Override
    public HasConfidence<ValueType, BaseType, RelativeTo> getAverage(
            Iterable<? extends HasConfidenceAndIsScalable<ValueType, BaseType, RelativeTo>> values, RelativeTo at) {
        if (values == null || Util.isEmpty(values)) {
            return null;
        } else {
            ScalableValue<ValueType, BaseType> numerator = null;
            double confidenceSum = 0;
            for (HasConfidenceAndIsScalable<ValueType, BaseType, RelativeTo> next : values) {
                double relativeWeight = (getWeigher() == null ? 1.0 : getWeigher().getConfidence(next.getRelativeTo(), at)) * next.getConfidence();
                ScalableValue<ValueType, BaseType> weightedNext = next.getScalableValue().multiply(relativeWeight);
                if (numerator == null) {
                    numerator = weightedNext;
                } else {
                    numerator = numerator.add(weightedNext);
                }
                confidenceSum += relativeWeight;
            }
            // TODO consider greater variance to reduce the confidence
            double newConfidence = confidenceSum / Util.size(values);
            BaseType result = numerator.divide(confidenceSum);
            return new HasConfidenceImpl<ValueType, BaseType, RelativeTo>(result, newConfidence, at);
        }
    }

    protected Weigher<RelativeTo> getWeigher() {
        return weigher;
    }
}
