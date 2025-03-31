package com.sap.sse.common.settings.value;

import java.math.BigDecimal;

public class DecimalValue extends AbstractValue<BigDecimal> {
    private static final long serialVersionUID = -2607692358788082588L;

    protected DecimalValue() {
    }

    public DecimalValue(BigDecimal value) {
        super(value);
    }

}
