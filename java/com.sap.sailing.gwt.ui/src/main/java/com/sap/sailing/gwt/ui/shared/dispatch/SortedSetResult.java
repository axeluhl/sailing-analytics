package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class SortedSetResult<T extends DTO> implements DTO, Result {
    private TreeSet<T> values = new TreeSet<>();
    
    @SuppressWarnings("unused")
    private SortedSetResult() {
    }

    public SortedSetResult(Collection<T> values) {
        super();
        this.values.addAll(values);
    }
    
    public Set<T> getValues() {
        return values;
    }
    
    public boolean isEmpty() {
        return values.isEmpty();
    }
}
