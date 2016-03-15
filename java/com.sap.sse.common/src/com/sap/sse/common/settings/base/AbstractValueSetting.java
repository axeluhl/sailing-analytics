package com.sap.sse.common.settings.base;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.ValueConverter;
import com.sap.sse.common.settings.ValueSetting;

public class AbstractValueSetting<T> extends AbstractHasValueSetting<T> implements ValueSetting<T> {
    
    private T defaultValue;
    
    private T value;

    protected AbstractValueSetting(String name, AbstractSettings settings, T defaultValue, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean isDefaultValue() {
        if(value == defaultValue) {
            return true;
        }
        if(value != null) {
            return value.equals(defaultValue);
        }
        // value == null && defaultValue != null => value isn't default
        return false;
    }
    
    @Override
    public String toString() {
        return "" +  value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        AbstractValueSetting other = (AbstractValueSetting) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
