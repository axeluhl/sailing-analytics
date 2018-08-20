package com.sap.sse.common.settings.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.common.settings.value.SettingsValue;
import com.sap.sse.common.settings.value.Value;
import com.sap.sse.common.settings.value.ValueCollectionValue;
import com.sap.sse.common.settings.value.ValueListValue;


public class SettingsList<T extends AbstractGenericSerializableSettings> extends AbstractSetting implements SettingsListSetting<T> {
    
    private SettingsFactory<T> settingsFactory;
    private final List<T> values = new ArrayList<>();
    
    public SettingsList(String name, AbstractGenericSerializableSettings settings, SettingsFactory<T> settingsFactory) {
        super(name, settings);
        this.settingsFactory = settingsFactory;
        adoptValue();
    }
    
    protected void adoptValue() {
        values.clear();
        ValueListValue value = getValue();
        if(value != null) {
            for(Value val : value.getValueObjects()) {
                T childSettingsInstance = settingsFactory.newInstance();
                childSettingsInstance.adoptValue((SettingsValue)val);
                values.add(childSettingsInstance);
            }
        }
    }

    @Override
    public SettingsFactory<T> getSettingsFactory() {
        return settingsFactory;
    }
    
    private ValueCollectionValue<List<Value>> ensureValue() {
        ValueListValue result = getValue();
        if(result == null) {
            result = new ValueListValue();
            settings.setValue(settingName, result);
        }
        return result;
    }
    
    private ValueListValue getValue() {
        return (ValueListValue) settings.getValue(settingName);
    }

    @Override
    public String toString() {
        return getValues().toString();
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
        settings.setValue(settingName, null);
    }

    @Override
    public Iterable<T> getValues() {
        return Collections.unmodifiableCollection(values);
    }

    @Override
    public void setValues(Iterable<T> values) {
        this.values.clear();
        ValueCollectionValue<List<Value>> valueObject = ensureValue();
        valueObject.clear();
        if(values != null) {
            for(T value : values) {
                this.values.add(value);
                valueObject.addValue(value.getInnerValueObject());
            }
        }
    }
    
    @Override
    public void addValue(T value) {
        ValueCollectionValue<List<Value>> valueObject = ensureValue();
        this.values.add(value);
        valueObject.addValue(value.getInnerValueObject());
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
        SettingsList other = (SettingsList) obj;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }
}
