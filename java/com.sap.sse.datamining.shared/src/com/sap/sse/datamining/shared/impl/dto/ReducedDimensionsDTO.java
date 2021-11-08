package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ReducedDimensionsDTO implements Serializable {
    private static final long serialVersionUID = 6874154208776613306L;
    
    final HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> reducedDimensions;
    final HashMap<FunctionDTO, FunctionDTO> fromOriginalDimensionToReducedDimension;
    
    public ReducedDimensionsDTO(HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> reducedDimensions,
            HashMap<FunctionDTO, FunctionDTO> fromOriginalDimensionToReducedDimension) {
        super();
        this.reducedDimensions = reducedDimensions;
        this.fromOriginalDimensionToReducedDimension = fromOriginalDimensionToReducedDimension;
    }
    
    public HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> getReducedDimensions() {
        return reducedDimensions;
    }
    
    public FunctionDTO getReducedDimension(FunctionDTO originalDimension) {
        return fromOriginalDimensionToReducedDimension.get(originalDimension);
    }
    
    public Map<FunctionDTO, FunctionDTO> getFromOriginalToReducedDimension() {
        return Collections.unmodifiableMap(fromOriginalDimensionToReducedDimension);
    }
}
