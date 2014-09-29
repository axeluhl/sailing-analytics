package com.sap.sse.datamining.impl.functions.criterias;

import com.sap.sse.datamining.factories.FunctionDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class FunctionMatchesDTOFilterCriterion extends AbstractFunctionFilterCriterion {

    private FunctionDTO functionDTOToMatch;

    public FunctionMatchesDTOFilterCriterion(FunctionDTO functionDTOToMatch) {
        this.functionDTOToMatch = functionDTOToMatch;
    }

    @Override
    public boolean matches(Function<?> function) {
        FunctionDTO functionDTO = FunctionDTOFactory.createFunctionDTO(function);
        return functionDTOToMatch.equals(functionDTO);
    }

}
