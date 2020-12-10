package com.sap.sse.common.settings.value;

import java.util.HashMap;

public class SettingsListValue {
    private HashMap<String, Value> values = new HashMap<>();
    
    public SettingsListValue() {
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
}
