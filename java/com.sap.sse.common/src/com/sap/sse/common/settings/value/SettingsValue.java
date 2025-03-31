package com.sap.sse.common.settings.value;

import java.util.Collections;
import java.util.HashMap;

public class SettingsValue implements Value {
    private static final long serialVersionUID = -3407605464729611500L;
    
    /**
     * All access to this map must be {@code synchronized}. Unfortunately, GWT does not offer
     * {@link Collections#synchronizedMap(java.util.Map)} in its JRE emulation, so we have to
     * make sure to consistently wrap all methods that access this map with a {@code synchronized}
     * block that obtains this map's monitor.
     */
    private HashMap<String, Value> values = new HashMap<>();
    
    public SettingsValue() {
    }

    public Value getValue(String name) {
        synchronized (values) {
            return values.get(name);
        }
    }
    
    public void setValue(String name, Value value) {
        synchronized (values) {
            if (value == null) {
                values.remove(name);
            } else {
                values.put(name, value);
            }
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
            SettingsValue other = (SettingsValue) obj;
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
