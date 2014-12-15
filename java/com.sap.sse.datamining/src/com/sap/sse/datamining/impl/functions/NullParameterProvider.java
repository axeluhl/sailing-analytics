package com.sap.sse.datamining.impl.functions;

import com.sap.sse.datamining.functions.ParameterProvider;

public class NullParameterProvider implements ParameterProvider {

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

}
