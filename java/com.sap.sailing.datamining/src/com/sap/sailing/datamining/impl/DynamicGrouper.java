package com.sap.sailing.datamining.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class DynamicGrouper<DataType> extends AbstractGrouper<DataType> {
    
    private String scriptText;
    private Binding binding;

    public DynamicGrouper(String scriptText) {
        super();
        this.scriptText = scriptText;
        binding = new Binding();
    }

    @Override
    protected GroupKey getGroupKeyFor(DataType dataEntry) {
        binding.setVariable("data", dataEntry);
        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate(scriptText);
        return new GenericGroupKey<Object>(value);
    }

}
