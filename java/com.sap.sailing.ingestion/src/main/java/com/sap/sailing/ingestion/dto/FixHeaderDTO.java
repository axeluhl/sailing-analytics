package com.sap.sailing.ingestion.dto;

import java.io.Serializable;
import java.util.List;

public class FixHeaderDTO implements Serializable {
    private static final long serialVersionUID = -6871581519012495468L;

    private String deviceUuid;
    private List<Object> fixes;

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public List<Object> getFixes() {
        return fixes;
    }

    public void setFixes(List<Object> fixes) {
        this.fixes = fixes;
    }
}
