package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sse.common.Util;

public class ValueListFilterParameter extends AbstractParameterizedDimensionFilter {
    private static final long serialVersionUID = -8440835683986197499L;
    
    private static final Logger logger = Logger.getLogger(ValueListFilterParameter.class.getName());

    private HashSet<? extends Serializable> values;
    
    private transient Set<ParameterModelListener> parameterModelListeners;

    @Deprecated // GWT serialization only
    ValueListFilterParameter() { }

    public <T extends Serializable> ValueListFilterParameter(String name, String typeName, Iterable<T> values) {
        super(name, typeName);
        logger.info("Creating parameter "+name+" of type "+typeName+" with values "+values);
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
        logger.info("Setting value of parameter "+getName()+" from "+oldValues+" to "+newValue);
        if (Util.isEmpty(newValue)) {
            logger.info("Setting value of parameter "+getName()+" to empty set");
        }
        final HashSet<T> set = new HashSet<>();
        Util.addAll(newValue, set);
        this.values = set;
        // iterate on a copy because listeners may choose to unregister due to the event being sent
        new HashSet<>(getParameterModelListeners()).forEach(l->l.parameterValueChanged(this, oldValues));
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
