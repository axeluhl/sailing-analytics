package com.sap.sse.datamining.shared.impl.dto.parameters;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class EndsWithFilterParameter extends TextConstrainedFilterParameter {

    public EndsWithFilterParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, String constraint) {
        super(retrieverLevel, dimension, constraint);
    }

    @Override
    protected boolean matches(String valueString) {
        return valueString.endsWith(constraint);
    }

}
