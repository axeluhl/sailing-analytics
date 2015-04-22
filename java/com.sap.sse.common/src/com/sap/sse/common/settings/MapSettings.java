package com.sap.sse.common.settings;

import java.util.Map;

public class MapSettings extends AbstractSettings {
    private final Map<String, Setting> settingsMap;
    
    protected MapSettings(Map<String, Setting> settingsMap) {
        this.settingsMap = settingsMap;
    }

    @Override
    public Map<String, Setting> getNonDefaultSettings() {
        return settingsMap;
    }
}
