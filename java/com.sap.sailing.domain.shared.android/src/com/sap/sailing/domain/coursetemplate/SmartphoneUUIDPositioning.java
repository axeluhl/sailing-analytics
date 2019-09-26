package com.sap.sailing.domain.coursetemplate;

import java.util.UUID;

public interface SmartphoneUUIDPositioning extends Positioning {
    UUID getDeviceUUID();
}
