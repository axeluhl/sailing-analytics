package com.sap.sse.common.settings.value;

import java.util.Collection;
import java.util.Collections;

import com.sap.sse.common.settings.generic.ValueConverter;

public abstract class ValueCollectionValue<C extends Collection<Value>> implements ValueCollection {
    private static final long serialVersionUID = -5820765644801217519L;
    private C values;
    
    protected ValueCollectionValue(C values) {
        this.values = values;
    }
    
    protected abstract <T> Collection<T> emptyCollection();
    
    @Override
    public <T> Collection<T> getValues(ValueConverter<T> converter) {
        Collection<T> result = emptyCollection();
        for (Value value : values) {
            result.add(converter.fromValue(value));
        }
        return result;
    }
    
    public Iterable<Value> getValueObjects() {
        return Collections.unmodifiableCollection(values);
    }
    
    @Override
    public <T> void setValues(Iterable<T> values, ValueConverter<T> converter) {
        clear();
        if(values != null) {
            for (T value : values) {
                addValue(value, converter);
            }
        }
    }
    
    @Override
    public void clear() {
        this.values.clear();
    }
    
    public <T> void addValue(T value, ValueConverter<T> converter) {
        this.values.add(converter.toValue(value));
    }
    
    public <T> void addValue(Value value) {
        this.values.add(value);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueCollectionValue<?> other = (ValueCollectionValue<?>) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return values.toString();
    }
}
