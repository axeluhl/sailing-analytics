package com.sap.sse.common.settings;

public class StringSetting implements Setting {
    private final String string;

    public StringSetting(String string) {
        super();
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public SettingType getType() {
        return SettingType.STRING;
    }
}
