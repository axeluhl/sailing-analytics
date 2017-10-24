package com.sap.sailing.expeditionconnector;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sse.common.impl.NamedImpl;

public class ExpeditionDeviceConfiguration extends NamedImpl implements Serializable {
    private static final long serialVersionUID = -7819154195403387909L;

    private final UUID deviceUuid;
    
    /**
     * The ID listed as first element in a UDP message coming from Expedition, prefixed by a '#'
     * character.
     */
    private final int expeditionBoatId;

    public ExpeditionDeviceConfiguration(String name, UUID deviceUuid, int expeditionBoatId) {
        super(name);
        this.deviceUuid = deviceUuid;
        this.expeditionBoatId = expeditionBoatId;
    }

    public UUID getDeviceUuid() {
        return deviceUuid;
    }

    public int getExpeditionBoatId() {
        return expeditionBoatId;
    }
}
