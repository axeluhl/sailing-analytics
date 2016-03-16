package com.sap.sse.common.settings.base;

import java.util.Collection;
import java.util.Collections;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.ValueCollectionSetting;
import com.sap.sse.common.settings.ValueConverter;

public abstract class AbstractValueCollectionSetting<T> extends AbstractHasValueSetting<T> implements ValueCollectionSetting<T> {
    
    public AbstractValueCollectionSetting(String name, AbstractSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
    }
    
    protected abstract Collection<T> getInnerCollection();
    
    @Override
    public Iterable<T> getValues() {
        return Collections.unmodifiableCollection(getInnerCollection());
    }

    @Override
    public final void setValues(Iterable<T> values) {
        this.getInnerCollection().clear();
        if(values != null) {
            for(T value : values) {
                this.getInnerCollection().add(value);
            }
        }
    }
    
    public void addValue(T value) {
        this.getInnerCollection().add(value);
    }

    public void clear() {
        this.getInnerCollection().clear();
    }

    @Override
    public String toString() {
        return getInnerCollection().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInnerCollection() == null) ? 0 : getInnerCollection().hashCode());
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
        @SuppressWarnings("rawtypes")
        AbstractValueCollectionSetting other = (AbstractValueCollectionSetting) obj;
        if (getInnerCollection() == null) {
            if (other.getInnerCollection() != null)
                return false;
        } else if (!getInnerCollection().equals(other.getInnerCollection()))
            return false;
        return true;
    }
}
