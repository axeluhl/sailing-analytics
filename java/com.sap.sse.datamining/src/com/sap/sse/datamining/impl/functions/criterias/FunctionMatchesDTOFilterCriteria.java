package com.sap.sse.datamining.impl.functions.criterias;

import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class FunctionMatchesDTOFilterCriteria implements FilterCriteria<Function<?>> {

    private FunctionDTO functionDTOToMatch;

    public FunctionMatchesDTOFilterCriteria(FunctionDTO functionDTOToMatch) {
        this.functionDTOToMatch = functionDTOToMatch;
    }

    @Override
    public boolean matches(Function<?> function) {
        FunctionDTO functionDTO = FunctionDTOFactory.createFunctionDTO(function);
        return functionDTOToMatch.equals(functionDTO);
    }

}
