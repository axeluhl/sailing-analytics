package com.sap.sse.common.settings;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractSettings implements Settings {
    @Override
    public SettingType getType() {
        return SettingType.MAP;
    }

    /**
     * This default implementation returns an empty map.
     */
    @Override
    public Map<String, Setting> getNonDefaultSettings() {
        return Collections.emptyMap();
    }
    
    public boolean equals(Object other) {
        return getNonDefaultSettings().equals(((Settings) other).getNonDefaultSettings());
    }

    @Override
    public String toString() {
        return getNonDefaultSettings().toString();
    }
}
