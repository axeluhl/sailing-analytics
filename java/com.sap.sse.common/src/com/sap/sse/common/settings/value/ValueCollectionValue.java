package com.sap.sse.common.settings.value;

import java.util.Collection;
import java.util.Collections;

import com.sap.sse.common.settings.generic.ValueConverter;

public abstract class ValueCollectionValue<C extends Collection<Value>> implements ValueCollection {
    private static final long serialVersionUID = -5820765644801217519L;

    /**
     * All access to this collection must be {@code synchronized}. Unfortunately, GWT does not offer
     * {@link Collections#synchronizedCollection(Collection)} in its JRE emulation, so we have to
     * make sure to consistently wrap all methods that access this collection with a {@code synchronized}
     * block that obtains this collection's monitor.
     */
    private C values;
    
    protected ValueCollectionValue(C values) {
        this.values = values;
    }
    
    protected abstract <T> Collection<T> emptyCollection();
    
    @Override
    public <T> Collection<T> getValues(ValueConverter<T> converter) {
        final Collection<T> result = emptyCollection();
        synchronized (values) {
            for (Value value : values) {
                result.add(converter.fromValue(value));
            }
            return result;
        }
    }
    
    public Iterable<Value> getValueObjects() {
        final Collection<Value> result = emptyCollection();
        synchronized (values) {
            result.addAll(values);
        }
        return result;
    }
    
    @Override
    public <T> void setValues(Iterable<T> values, ValueConverter<T> converter) {
        synchronized (this.values) {
            clear();
            if (values != null) {
                for (T value : values) {
                    addValue(value, converter);
                }
            }
        }
    }
    
    @Override
    public void clear() {
        synchronized (values) {
            this.values.clear();
        }
    }
    
    public <T> void addValue(T value, ValueConverter<T> converter) {
        synchronized (values) {
            addValue(converter.toValue(value));
        }
    }
    
    public <T> void addValue(Value value) {
        synchronized (values) {
            this.values.add(value);
        }
    }

    public boolean isEmpty() {
        synchronized (values) {
            return values.isEmpty();
        }
    }

    public int size() {
        synchronized (values) {
            return values.size();
        }
    }

    @Override
    public int hashCode() {
        synchronized (values) {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((values == null) ? 0 : values.hashCode());
            return result;
        }
    }

    @Override
    public boolean equals(Object obj) {
        synchronized (values) {
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
    }
    
    @Override
    public String toString() {
        synchronized (values) {
            return values.toString();
        }
    }
}
