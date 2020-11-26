package com.sap.sailing.ingestion.dto;

import java.io.Serializable;
import java.util.List;

public class EndpointDTO implements Serializable {
	
	private static final long serialVersionUID = 3115461658787136449L;

	private String endpointUuid;
	private String endpointCallbackUrl;
	private List<String> devicesUuid;
	
	public String getEndpointUuid() {
		return endpointUuid;
	}
	
	public void setEndpointUuid(String endpointUuid) {
		this.endpointUuid = endpointUuid;
	}
	
	public String getEndpointCallbackUrl() {
		return endpointCallbackUrl;
	}
	
	public void setEndpointCallbackUrl(String endpointCallbackUrl) {
		this.endpointCallbackUrl = endpointCallbackUrl;
	}
	
	public List<String> getDevicesUuid() {
		return devicesUuid;
	}
	
	public void setDevicesUuid(List<String> devicesUuid) {
		this.devicesUuid = devicesUuid;
	}
	
}
