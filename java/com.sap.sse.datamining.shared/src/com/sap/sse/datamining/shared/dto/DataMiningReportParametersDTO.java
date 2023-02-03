package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A parameter model for a {@link DataMiningReportDTO}. The 
 * @author Axel Uhl (d043530)
 *
 */
public interface DataMiningReportParametersDTO extends Serializable {
    boolean isEmpty();
    
    /**
     * @return a non-live copy of the parameters
     */
    HashSet<FilterDimensionParameter> getAll();
    
    /**
     * @return {@code true} if and only if a parameter that {@link Object#equals(Object) equals} {@code parameter} is
     *         contained in this parameter set.
     */
    boolean contains(FilterDimensionParameter parameter);
    
    HashMap<Integer, HashSet<FilterDimensionParameter>> getAllUsages();
    
    /**
     * @param key
     *            the zero-based index of the result presenter tab that is the context for the parameter set requested
     */
    HashSet<FilterDimensionParameter> getUsages(Integer key);
    
    boolean hasUsages();
    
    /**
     * Tells whether any parameter of this object is used in the result presenter panel identified by the
     * zero-based {@code key} index.
     */
    boolean hasUsages(Integer key);
    boolean isUsed(FilterDimensionParameter parameter);
}
