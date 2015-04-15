package com.sap.sse.common.settings;

import java.math.BigDecimal;

public class NumberSetting implements Setting {
    private final BigDecimal number;

    public NumberSetting(BigDecimal number) {
        super();
        this.number = number;
    }

    public BigDecimal getNumber() {
        return number;
    }

    @Override
    public SettingType getType() {
        return SettingType.NUMBER;
    }
}
