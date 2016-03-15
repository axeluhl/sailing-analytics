package com.sap.sse.common.settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSettings extends AbstractSetting implements Settings {
    
    private Map<String, Setting> childSettings = new HashMap<>();
    
    public AbstractSettings() {
    }
    
    public AbstractSettings(String name, AbstractSettings settings) {
        super(name, settings);
    }
    
    protected void addSetting(String name, Setting setting) {
        if(childSettings.containsKey(name)) {
            throw new IllegalArgumentException("setting with name " + name + " already added to setting type " + getClass().getSimpleName());
        }
        childSettings.put(name, setting);
    }

    @Override
    public boolean isDefaultValue() {
        for(Setting setting : childSettings.values()) {
            if(!setting.isDefaultValue()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Map<String, Setting> getChildSettings() {
        return Collections.unmodifiableMap(childSettings);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("{(").append(this.getClass().getSimpleName()).append(")");
        boolean first = true;
        for(Map.Entry<String, Setting> childSetting : childSettings.entrySet()) {
            if(!first) {
                sb.append("; ");
            }
            sb.append(childSetting.getKey() + "=" + childSetting.getValue());
            
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((childSettings == null) ? 0 : childSettings.hashCode());
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
        AbstractSettings other = (AbstractSettings) obj;
        if (childSettings == null) {
            if (other.childSettings != null)
                return false;
        } else if (!childSettings.equals(other.childSettings))
            return false;
        return true;
    }
}
