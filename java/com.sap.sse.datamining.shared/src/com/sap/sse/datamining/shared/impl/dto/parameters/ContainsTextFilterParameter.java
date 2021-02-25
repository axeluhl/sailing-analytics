package com.sap.sse.datamining.shared.impl.dto.parameters;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class ContainsTextFilterParameter extends TextConstrainedFilterParameter {
    private static final long serialVersionUID = 3323982237823278947L;

    public  ContainsTextFilterParameter() { }

    public ContainsTextFilterParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, String constraint) {
        super(retrieverLevel, dimension, constraint);
    }

    @Override
    protected boolean matches(String valueString) {
        return valueString.contains(getConstraint());
    }

}
