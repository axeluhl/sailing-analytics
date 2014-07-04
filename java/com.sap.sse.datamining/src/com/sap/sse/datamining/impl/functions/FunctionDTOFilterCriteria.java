package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class FunctionDTOFilterCriteria implements FilterCriteria<Function<?>> {

    private FunctionDTO functionDTOToMatch;

    public FunctionDTOFilterCriteria(FunctionDTO functionDTOToMatch) {
        this.functionDTOToMatch = functionDTOToMatch;
    }

    @Override
    public boolean matches(Function<?> function) {
        FunctionDTO functionDTO = function.asDTO();
        return functionDTOToMatch.equals(functionDTO);
    }

}
