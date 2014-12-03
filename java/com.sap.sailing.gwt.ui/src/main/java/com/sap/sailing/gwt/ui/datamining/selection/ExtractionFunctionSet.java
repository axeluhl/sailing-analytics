package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class ExtractionFunctionSet {
    
    private final Map<FunctionDTOKey, FunctionDTO> functionDTOMap;
    private final Map<StrippedFunctionDTO, Collection<String>> sourceTypeNamesMap;
    
    public ExtractionFunctionSet() {
        functionDTOMap = new HashMap<>();
        sourceTypeNamesMap = new HashMap<>();
    }

    public void addAll(Collection<FunctionDTO> functionDTOs) {
        for (FunctionDTO functionDTO : functionDTOs) {
            add(functionDTO);
        }
    }
    
    private void add(FunctionDTO functionDTO) {
        String sourceTypeName = functionDTO.getSourceTypeName();
        StrippedFunctionDTO strippedFunctionDTO = new StrippedFunctionDTO(functionDTO);
        FunctionDTOKey key = new FunctionDTOKey(strippedFunctionDTO, sourceTypeName);
        
        if (!functionDTOMap.containsKey(key)) {
            functionDTOMap.put(key, functionDTO);
            
            if (!sourceTypeNamesMap.containsKey(strippedFunctionDTO)) {
                sourceTypeNamesMap.put(strippedFunctionDTO, new HashSet<String>());
            }
            sourceTypeNamesMap.get(strippedFunctionDTO).add(sourceTypeName);
        }
    }

    public Collection<StrippedFunctionDTO> getStrippedFunctionDTOs() {
        return sourceTypeNamesMap.keySet();
    }
    
    public Collection<String> getSourceTypeNames(StrippedFunctionDTO strippedFunctionDTO) {
        return sourceTypeNamesMap.get(strippedFunctionDTO);
    }
    
    public FunctionDTO getFunctionDTO(StrippedFunctionDTO strippedFunctionDTO, String sourceTypeName) {
        FunctionDTOKey key = new FunctionDTOKey(strippedFunctionDTO, sourceTypeName);
        return functionDTOMap.get(key);
    }
    
    public void clear() {
        functionDTOMap.clear();
        sourceTypeNamesMap.clear();
    }
    
    private class FunctionDTOKey extends Pair<StrippedFunctionDTO, String> {
        private static final long serialVersionUID = -5703889518055076338L;

        public FunctionDTOKey(StrippedFunctionDTO a, String b) {
            super(a, b);
        }
        
    }

}
