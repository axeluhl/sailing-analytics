package com.sap.sse.gwt.dispatch.shared.commands;

import java.util.Collection;

public abstract class AbstractCollectionResult<C extends Collection<T>, T extends DTO> implements CollectionResult<T> {

    private final C collection;
    
    public AbstractCollectionResult(C collection, Collection<T> values) {
        this.collection = collection;
        if(values != null) {
            collection.addAll(values);
        }
    }
    
    /**
     * Adds the {@code value} to this result if {@code value} is not {@code null}. If an object equal
     * to {@code value} is already contained in this result, no action is taken.
     */
    public void addValue(T value) {
        if (value != null) {
            this.collection.add(value);
        }
    }
    
    public C getValues() {
        return collection;
    }
    
    public boolean isEmpty() {
        return collection.isEmpty();
    }

}
