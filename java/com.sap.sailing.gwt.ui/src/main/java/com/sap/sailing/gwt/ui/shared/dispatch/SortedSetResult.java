package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SortedSetResult<T extends DTO> implements DTO, Result {
    private TreeSet<T> values = new TreeSet<>();
    
    public SortedSetResult() {
    }

    public SortedSetResult(Collection<T> values) {
        this.values.addAll(values);
    }
    
    public void addValue(T value) {
        if (value != null) {
            this.values.add(value);
        }
    }
    
    public Set<T> getValues() {
        return values;
    }
    
    public boolean isEmpty() {
        return values.isEmpty();
    }
}
