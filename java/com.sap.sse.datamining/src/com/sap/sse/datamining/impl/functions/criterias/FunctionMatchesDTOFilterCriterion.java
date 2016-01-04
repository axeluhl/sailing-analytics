package com.sap.sse.datamining.impl.functions.criterias;

import com.sap.sse.datamining.factories.DataMiningDTOFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class FunctionMatchesDTOFilterCriterion extends AbstractFunctionFilterCriterion {

    private FunctionDTO functionDTOToMatch;
    private DataMiningDTOFactory functionDTOFactory;

    public FunctionMatchesDTOFilterCriterion(DataMiningDTOFactory functionDTOFactory, FunctionDTO functionDTOToMatch) {
        this.functionDTOToMatch = functionDTOToMatch;
        this.functionDTOFactory = functionDTOFactory;
    }

    @Override
    public boolean matches(Function<?> function) {
        FunctionDTO functionDTO = functionDTOFactory.createFunctionDTO(function);
        return functionDTOToMatch.equals(functionDTO);
    }

}
