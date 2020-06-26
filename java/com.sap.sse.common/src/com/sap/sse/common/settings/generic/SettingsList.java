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

    /**
     * All access to this list must be {@code synchronized}. Unfortunately, GWT does not offer
     * {@link Collections#synchronizedList(List)} in its JRE emulation, so we have to
     * make sure to consistently wrap all methods that access this list with a {@code synchronized}
     * block that obtains this list's monitor.
     */
    private final List<T> values = new ArrayList<>();
    
    public SettingsList(String name, AbstractGenericSerializableSettings settings, SettingsFactory<T> settingsFactory) {
        super(name, settings);
        this.settingsFactory = settingsFactory;
        adoptValue();
    }
    
    protected void adoptValue() {
        synchronized (values) {
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
    }

    @Override
    public SettingsFactory<T> getSettingsFactory() {
        return settingsFactory;
    }
    
    private ValueCollectionValue<List<Value>> ensureValue() {
        ValueListValue result = getValue();
        if (result == null) {
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
        synchronized (values) {
            // explicit default values are possible to implement
            // currently, empty is always default
            return values.isEmpty();
        }
    }

    @Override
    public void resetToDefault() {
        synchronized (values) {
            this.values.clear();
            settings.setValue(settingName, null);
        }
    }

    @Override
    public Iterable<T> getValues() {
        synchronized (values) {
            final List<T> result = new ArrayList<>(values);
            return result;
        }
    }

    @Override
    public void setValues(Iterable<T> values) {
        synchronized (this.values) {
            this.values.clear();
            ValueCollectionValue<List<Value>> valueObject = ensureValue();
            valueObject.clear();
            if (values != null) {
                for (T value : values) {
                    this.values.add(value);
                    valueObject.addValue(value.getInnerValueObject());
                }
            }
        }
    }
    
    @Override
    public void addValue(T value) {
        synchronized (values) {
            ValueCollectionValue<List<Value>> valueObject = ensureValue();
            this.values.add(value);
            valueObject.addValue(value.getInnerValueObject());
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
}
