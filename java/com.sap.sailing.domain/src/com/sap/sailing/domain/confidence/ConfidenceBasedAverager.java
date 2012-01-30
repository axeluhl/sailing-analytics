package com.sap.sailing.domain.confidence;

public interface ConfidenceBasedAverager<ValueType, BaseType, RelativeTo> {
    /**
     * If a non-<code>null</code> weigher has been set for this averager, <code>at</code> must be a valid
     * reference point to which the weigher will determine the difference and from it the confidence for each
     * of the <code>values</code>. Otherwise, the <code>at</code> argument is ignored.
     */
    HasConfidence<ValueType, BaseType, RelativeTo> getAverage(
            Iterable<? extends HasConfidenceAndIsScalable<ValueType, BaseType, RelativeTo>> values, RelativeTo at);
}
