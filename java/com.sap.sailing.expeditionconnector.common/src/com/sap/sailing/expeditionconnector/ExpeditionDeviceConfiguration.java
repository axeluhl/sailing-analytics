package com.sap.sailing.expeditionconnector;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sse.common.impl.NamedImpl;

public class ExpeditionDeviceConfiguration extends NamedImpl implements Serializable {
    private static final long serialVersionUID = -7819154195403387909L;

    private final UUID deviceUuid;
    
    /**
     * The ID listed as first element in a UDP message coming from Expedition, prefixed by a '#'
     * character. If {@code null}, no mapping currently exists for the device.
     */
    private final Integer expeditionBoatId;

    public ExpeditionDeviceConfiguration(String name, UUID deviceUuid, Integer expeditionBoatId) {
        super(name);
        this.deviceUuid = deviceUuid;
        this.expeditionBoatId = expeditionBoatId;
    }

    public UUID getDeviceUuid() {
        return deviceUuid;
    }

    public Integer getExpeditionBoatId() {
        return expeditionBoatId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((deviceUuid == null) ? 0 : deviceUuid.hashCode());
        result = prime * result + ((expeditionBoatId == null) ? 0 : expeditionBoatId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpeditionDeviceConfiguration other = (ExpeditionDeviceConfiguration) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (deviceUuid == null) {
            if (other.deviceUuid != null)
                return false;
        } else if (!deviceUuid.equals(other.deviceUuid))
            return false;
        if (expeditionBoatId == null) {
            if (other.expeditionBoatId != null)
                return false;
        } else if (!expeditionBoatId.equals(other.expeditionBoatId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpeditionDeviceConfiguration [deviceUuid=" + deviceUuid + ", expeditionBoatId=" + expeditionBoatId
                + ", getName()=" + getName() + "]";
    }
}
