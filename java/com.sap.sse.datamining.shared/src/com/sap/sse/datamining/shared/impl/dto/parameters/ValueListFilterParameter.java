package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;

public class ValueListFilterParameter extends AbstractParameterizedDimensionFilter {
    private static final long serialVersionUID = -8440835683986197499L;
    
    private HashSet<? extends Serializable> values;
    
    private transient Set<ParameterModelListener> parameterModelListeners;

    @Deprecated // GWT serialization only
    ValueListFilterParameter() { }

    public <T extends Serializable> ValueListFilterParameter(String name, String typeName, Iterable<T> values) {
        super(name, typeName);
        final HashSet<T> set = new HashSet<>();
        Util.addAll(values, set);
        this.values = set;
        this.parameterModelListeners = new HashSet<>();
    }
    
    @Override
    public Iterable<? extends Serializable> getValues() {
        return new HashSet<>(values);
    }

    @Override
    public <T extends Serializable> void setValues(Iterable<T> newValue) {
        final Iterable<? extends Serializable> oldValues = getValues();
        final HashSet<T> set = new HashSet<>();
        Util.addAll(newValue, set);
        this.values = set;
        getParameterModelListeners().forEach(l->l.parameterValueChanged(this, oldValues));
    }

    @Override
    public void addParameterModelListener(ParameterModelListener listener) {
        getParameterModelListeners().add(listener);
    }

    @Override
    public void removeParameterModelListener(ParameterModelListener listener) {
        getParameterModelListeners().remove(listener);
    }
    
    private Set<ParameterModelListener> getParameterModelListeners() {
        if (parameterModelListeners == null) { // transient; may have be nulled by de-serialization
            parameterModelListeners = new HashSet<>();
        }
        return parameterModelListeners;
    }
}
