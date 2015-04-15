package com.sap.sse.common.settings;

public abstract class AbstractSettings implements Settings {
    @Override
    public SettingType getType() {
        return SettingType.MAP;
    }
}
