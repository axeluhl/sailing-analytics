package com.sap.sse.common.settings.value;

import java.util.HashMap;

public class SettingsValue implements Value {
    private static final long serialVersionUID = -3407605464729611500L;
    private HashMap<String, Value> values = new HashMap<>();
    
    public SettingsValue() {
    }

    public Value getValue(String name) {
        return values.get(name);
    }
    
    public void setValue(String name, Value value) {
        if(value == null) {
            values.remove(name);
        } else {
            values.put(name, value);
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
        SettingsValue other = (SettingsValue) obj;
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
