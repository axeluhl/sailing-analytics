package com.sap.sse.datamining.impl.components.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;

/**
 * Along a retriever chain certain dimensions may seem redundant. In particular, if at some level there is a dimension
 * function that is a compound function whose first navigation step moves back up to the previous retriever level
 * through a connector step then obviously all the previous level's dimensions would be listed for the current
 * retriever level redundantly.<p>
 * 
 * Reducing dimensions therefore adds to the usability, removing such redundant dimensions. However, the dimensions
 * listed for the fact type (the data type of the last retriever level in the chain) will be what the user is
 * presented for grouping. But at the retriever level, many of those dimensions may have been removed during
 * the reduction process because they start with a navigation "up the retriever hierarchy" through a connector.
 * In order to trace back the dimensions available on the fact type to those dimensions offered in the filtering
 * process, this structure no only keeps a mapping from the retriever levels to the reduced set of dimensions
 * but also a mapping from the full set of dimensions to the corresponding mapping from the reduced set. This
 * way, the corresponding filter dimension can easily be identified in the reduced dimension set when any
 * original dimension is known.
 * 
 * @see FunctionManager#getReducedDimensionsMappedByLevelFor(com.sap.sse.datamining.components.DataRetrieverChainDefinition)
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ReducedDimensions {
    private final Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> reducedDimensions;
    
    /**
     * Has all dimensions as keys that one gets when collecting all dimensions from the result of
     * {@link FunctionManager#getDimensionsMappedByLevelFor(com.sap.sse.datamining.components.DataRetrieverChainDefinition)}.
     * The values are all contained in at least one of the values of {@link #reducedDimensions}.
     */
    private final Map<Function<?>, Function<?>> fromOriginalDimensionToReducedDimension;

    public ReducedDimensions(Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> reducedDimensions,
            Map<Function<?>, Function<?>> fromOriginalDimensionToReducedDimension) {
        super();
        this.reducedDimensions = reducedDimensions;
        this.fromOriginalDimensionToReducedDimension = fromOriginalDimensionToReducedDimension;
    }
    
    /**
     * Constructs an empty object of this type with no dimensions in it
     */
    public ReducedDimensions() {
        this(new HashMap<>(), new HashMap<>());
    }

    public Function<?> getReducedDimension(Function<?> originalDimension) {
        return fromOriginalDimensionToReducedDimension.get(originalDimension);
    }
    
    public Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> getReducedDimensions() {
        return Collections.unmodifiableMap(reducedDimensions);
    }
    
    public Map<Function<?>, Function<?>> getFromOriginalDimensionToReducedDimension() {
        return Collections.unmodifiableMap(fromOriginalDimensionToReducedDimension);
    }

    /**
     * Produces a new object of this type by first copying this objects contents, then merging {@code other}'s contents.
     * During the merge, distinct retriever levels are expected (asserted by an {@code assert} statement). The
     * {@code replaceExistingMappingsFromOriginalToReducedDimension} decides what to do if a mapping from an original
     * dimension {@code O} to a reduced dimension {@code R_this} already exists in this object and {@code other} also has a
     * mapping for the same original dimension {@code O} to another reduced dimension {@code R_other}. If the
     * {@code replaceExistingMappingsFromOriginalToReducedDimension} parameter is false, the mapping from {@code O} to
     * {@code R_this} as defined in this object is copied to the result. Otherwise, if this object has a mapping from
     * {@code R_other} to, say {@code R2_this} then a mapping from {@code O} to {@code R2_this} is added to the result,
     * causing a transitive mapping from {@code other}'s {@code O} dimension.
     * 
     * @param other
     *            the content
     * @return the object created
     */
    public ReducedDimensions createByAdd(ReducedDimensions other, boolean replaceExistingMappingsFromOriginalToReducedDimension) {
        final Map<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> newReducedDimensions = new HashMap<>(this.reducedDimensions);
        final Map<Function<?>, Function<?>> newFromOriginalDimensionToReducedDimension = new HashMap<>(this.fromOriginalDimensionToReducedDimension);
        for (final Entry<DataRetrieverLevel<?, ?>, Iterable<Function<?>>> rd : other.getReducedDimensions().entrySet()) {
            final boolean alreadyContained = newReducedDimensions.put(rd.getKey(), rd.getValue()) != null;
            assert !alreadyContained;
        }
        for (final Entry<Function<?>, Function<?>> f : other.getFromOriginalDimensionToReducedDimension().entrySet()) {
            if (replaceExistingMappingsFromOriginalToReducedDimension || !newFromOriginalDimensionToReducedDimension.containsKey(f.getKey())) {
                final Function<?> original = getReducedDimension(f.getValue());
                newFromOriginalDimensionToReducedDimension.put(f.getKey(), original == null ? f.getValue() : original);
            }
        }
        return new ReducedDimensions(newReducedDimensions, newFromOriginalDimensionToReducedDimension);
    }

    @Override
    public String toString() {
        return "ReducedDimensions [reducedDimensions=" + reducedDimensions
                + ", fromOriginalDimensionToReducedDimension=" + fromOriginalDimensionToReducedDimension + "]";
    }
}
