package com.sap.sailing.datamining.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class DynamicGrouper<DataType> extends AbstractGrouper<DataType> {
    
    private Script script;

    public DynamicGrouper(String scriptText) {
        super();
        script = new GroovyShell().parse(scriptText);
    }

    @Override
    protected GroupKey getGroupKeyFor(DataType dataEntry) {
        Binding binding = new Binding();
        binding.setVariable("data", dataEntry);
        script.setBinding(binding);
        Object value = script.run();
        return new GenericGroupKey<Object>(value);
    }

}
