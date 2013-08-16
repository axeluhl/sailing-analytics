package com.sap.sailing.datamining;

import groovy.lang.Binding;

public interface BaseBindingProvider<DataType> {
    
    /**
     * Creates a new binding with all necessary variables and types, to work on the atomar data.
     */
    public Binding createBaseBinding();

}
