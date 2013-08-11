package com.sap.sailing.datamining.impl;

public class StringGroupKey extends GenericGroupKey<String> {
    
    public StringGroupKey(String value) {
        super(value);
    }

    @Override
    public String asString() {
        return getValue();
    }

}
