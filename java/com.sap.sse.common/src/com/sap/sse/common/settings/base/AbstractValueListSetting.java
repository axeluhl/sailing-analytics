package com.sap.sse.common.settings.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.ValueConverter;
import com.sap.sse.common.settings.ValueListSetting;

public abstract class AbstractValueListSetting<T> extends AbstractHasValueSetting<T> implements ValueListSetting<T> {
    
    private List<T> values = new ArrayList<>();
    
    public AbstractValueListSetting(String name, AbstractSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
    }
    
    @Override
    public boolean isDefaultValue() {
        // explicit default values are possible to implement
        // currently, empty is always default
        return values.isEmpty();
    }
    
    @Override
    public Iterable<T> getValues() {
        return Collections.unmodifiableCollection(values);
    }

    @Override
    public void setValues(Iterable<T> values) {
        this.values.clear();
        if(values != null) {
            for(T value : values) {
                this.values.add(value);
            }
        }
    }
    
    public void addValue(T value) {
        this.values.add(value);
    }

    public void clear() {
        this.values.clear();
    }

    @Override
    public String toString() {
        return values.toString();
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
        @SuppressWarnings("rawtypes")
        AbstractValueListSetting other = (AbstractValueListSetting) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }
}
