package com.sap.sse.datamining.shared.impl.dto.parameters;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class StartsWithFilterParameter extends TextConstrainedFilterParameter {
    private static final long serialVersionUID = -1505267790252158436L;

    public  StartsWithFilterParameter() { }

    public StartsWithFilterParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, String constraint) {
        super(retrieverLevel, dimension, constraint);
    }

    @Override
    protected boolean matches(String valueString) {
        return valueString.startsWith(getConstraint());
    }

}
