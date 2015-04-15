package com.sap.sse.common.settings;

public class EnumSetting<T extends Enum<T>> implements Setting {
    private final Enum<T> value;

    public EnumSetting(Enum<T> value) {
        super();
        this.value = value;
    }

    public Enum<T> getValue() {
        return value;
    }
}
