package com.sap.sailing.domain.coursetemplate;

import java.util.UUID;

public interface StoredDeviceIdentifierPositioning extends Positioning {
    default UUID getDeviceUUID() {
        return null;
    }
}
