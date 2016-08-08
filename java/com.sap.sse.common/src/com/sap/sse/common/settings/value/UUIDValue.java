package com.sap.sse.common.settings.value;

import java.util.UUID;

public class UUIDValue extends AbstractValue<UUID> {
    private static final long serialVersionUID = 4218811543529632907L;

    protected UUIDValue() {
    }

    public UUIDValue(UUID value) {
        super(value);
    }

}
