package com.sap.sse.common;

import java.util.UUID;

public interface NamedWithUUID extends NamedWithID {
    @Override
    UUID getId();
}
