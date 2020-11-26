package com.sap.sailing.ingestion.dto;

import java.io.Serializable;
import java.util.List;

public class GpsFixDTO implements Serializable {

	private static final long serialVersionUID = -6871581519012495468L;
	
	private String deviceUuid;
	private List<GpsFixPayloadDTO> fixes;
	
	public String getDeviceUuid() {
		return deviceUuid;
	}
	
	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}
	
	public List<GpsFixPayloadDTO> getFixes() {
		return fixes;
	}
	
	public void setFixes(List<GpsFixPayloadDTO> fixes) {
		this.fixes = fixes;
	}

}
