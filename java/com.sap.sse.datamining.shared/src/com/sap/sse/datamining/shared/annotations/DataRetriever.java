package com.sap.sse.datamining.shared.annotations;

public @interface DataRetriever {
    
    public Class<?> dataType();
    
    public String groupName();
    
    public int level();

}
