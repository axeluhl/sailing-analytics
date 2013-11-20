package com.sap.sailing.datamining.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class DynamicGroupingWorker<DataType> extends AbstractGroupingWorker<DataType> {
    
    private Script script;
    private Binding binding;

    public DynamicGroupingWorker(String scriptText, BaseBindingProvider<DataType> baseBindingProvider) {
        super();
        script = new GroovyShell().parse(scriptText);
        binding = baseBindingProvider.createBaseBinding();
        script.setBinding(binding);
    }

    @Override
    protected GroupKey getGroupKeyFor(DataType dataEntry) {
        binding.setVariable("data", dataEntry);
        return new GenericGroupKey<Object>(script.run());
    }

}
