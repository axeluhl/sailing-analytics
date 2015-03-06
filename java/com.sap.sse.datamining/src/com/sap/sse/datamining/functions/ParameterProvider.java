package com.sap.sse.datamining.functions;

import com.sap.sse.datamining.impl.functions.NullParameterProvider;

public interface ParameterProvider {
    
    public static final ParameterProvider NULL = new NullParameterProvider();
    
    public Object[] getParameters();
    

}
