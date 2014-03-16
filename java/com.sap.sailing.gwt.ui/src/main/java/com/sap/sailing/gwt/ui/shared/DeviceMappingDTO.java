package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.Date;

public class DeviceMappingDTO implements Serializable {
    private static final long serialVersionUID = -3272980620254526040L;
    public String deviceType;
    public String deviceId;
    public Date from;
    public Date to;
    public Serializable mappedTo;
    
    protected DeviceMappingDTO() {}
    
    public DeviceMappingDTO(String deviceType, String deviceId, Date from, Date to, Serializable mappedTo) {
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.from = from;
        this.to = to;
        this.mappedTo = mappedTo;
    }
}
