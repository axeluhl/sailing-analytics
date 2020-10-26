package com.sap.sse.datamining.shared.impl.dto.parameters;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class StartsWithFilterParameter extends TextConstrainedFilterParameter {

    public StartsWithFilterParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, String constraint) {
        super(retrieverLevel, dimension, constraint);
    }

    @Override
    protected boolean matches(String valueString) {
        return valueString.startsWith(constraint);
    }

}
