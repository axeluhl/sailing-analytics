package com.sap.sse.common.settings.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.common.settings.AbstractSetting;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.CollectionSetting;

public abstract class AbstractListSetting<T> extends AbstractSetting implements CollectionSetting<T> {
    
    private final List<T> values = new ArrayList<>();
    
    public AbstractListSetting(String name, AbstractSettings settings) {
        super(name, settings);
    }
    
    public AbstractListSetting() {
    }
    
    @Override
    public boolean isDefaultValue() {
        // explicit default values are possible to implement
        // currently, empty is always default
        return values.isEmpty();
    }
    
    @Override
    public void resetToDefault() {
        this.values.clear();
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
        AbstractListSetting other = (AbstractListSetting) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }
}
