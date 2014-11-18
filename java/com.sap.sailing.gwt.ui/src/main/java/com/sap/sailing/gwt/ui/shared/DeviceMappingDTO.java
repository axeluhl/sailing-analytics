package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;

public class DeviceMappingDTO implements Serializable {
    private static final long serialVersionUID = -3272980620254526040L;
    public DeviceIdentifierDTO deviceIdentifier;
    public Date from;
    public Date to;
    public MappableToDevice mappedTo;
    public List<UUID> originalRaceLogEventIds;
    
    protected DeviceMappingDTO() {}
    
    public DeviceMappingDTO(DeviceIdentifierDTO deviceId, Date from, Date to, MappableToDevice mappedTo,
            List<UUID> originalRaceLogEventIds) {
        this.deviceIdentifier = deviceId;
        this.from = from;
        this.to = to;
        this.mappedTo = mappedTo;
        this.originalRaceLogEventIds = originalRaceLogEventIds;
    }
}
