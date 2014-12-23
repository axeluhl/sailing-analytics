package com.sap.sse.datamining.impl.functions;

import java.util.Collections;

public class NullParameterProvider extends SimpleParameterProvider {

    public NullParameterProvider() {
        super(Collections.emptyList(), new Object[0]);
    }

}
