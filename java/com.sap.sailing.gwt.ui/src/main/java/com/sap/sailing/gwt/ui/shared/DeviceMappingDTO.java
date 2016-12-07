package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sse.common.TimePoint;

public class DeviceMappingDTO implements IsSerializable {
    public DeviceIdentifierDTO deviceIdentifier;
    public Date from;
    public Date to;
    public MappableToDevice mappedTo;
    public List<UUID> originalRaceLogEventIds;
    public TimePoint lastFix;
    
    protected DeviceMappingDTO() {}
    
    /**
     * Produces an instance without a last fix
     */
    public DeviceMappingDTO(DeviceIdentifierDTO deviceId, Date from, Date to, MappableToDevice mappedTo,
            List<UUID> originalRaceLogEventIds) {
        this(deviceId, from, to, mappedTo, originalRaceLogEventIds, /* lastFix */ null);
    }
    
    public DeviceMappingDTO(DeviceIdentifierDTO deviceId, Date from, Date to, MappableToDevice mappedTo,
            List<UUID> originalRaceLogEventIds, TimePoint lastFix) {
        this.deviceIdentifier = deviceId;
        this.from = from;
        this.to = to;
        this.mappedTo = mappedTo;
        this.originalRaceLogEventIds = originalRaceLogEventIds;
        this.lastFix = lastFix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceIdentifier == null) ? 0 : deviceIdentifier.hashCode());
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((mappedTo == null) ? 0 : mappedTo.hashCode());
        result = prime * result + ((originalRaceLogEventIds == null) ? 0 : originalRaceLogEventIds.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
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
        DeviceMappingDTO other = (DeviceMappingDTO) obj;
        if (deviceIdentifier == null) {
            if (other.deviceIdentifier != null)
                return false;
        } else if (!deviceIdentifier.equals(other.deviceIdentifier))
            return false;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (mappedTo == null) {
            if (other.mappedTo != null)
                return false;
        } else if (!mappedTo.equals(other.mappedTo))
            return false;
        if (originalRaceLogEventIds == null) {
            if (other.originalRaceLogEventIds != null)
                return false;
        } else if (!originalRaceLogEventIds.equals(other.originalRaceLogEventIds))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        return true;
    }
}
