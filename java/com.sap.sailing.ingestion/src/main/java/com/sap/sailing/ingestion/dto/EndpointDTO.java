package com.sap.sailing.ingestion.dto;

import java.io.Serializable;
import java.util.List;

public class EndpointDTO implements Serializable {
    private static final long serialVersionUID = 3115461658787136449L;

    public final static String REGISTER_ACTION = "register";
    public final static String UNREGISTER_ACTION = "unregister";

    private String endpointUuid;
    /**
     * One of register or unregister
     */
    private String action;
    private String endpointCallbackUrl;
    private List<String> devicesUuid;

    public EndpointDTO(String endpointUuid, String action, String endpointCallbackUrl, List<String> devicesUuid) {
        super();
        this.endpointUuid = endpointUuid;
        this.action = action;
        this.endpointCallbackUrl = endpointCallbackUrl;
        this.devicesUuid = devicesUuid;
    }

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isRegisterAction() {
        return getAction().equalsIgnoreCase(REGISTER_ACTION);
    }

    public boolean isUnRegisterAction() {
        return getAction().equalsIgnoreCase(UNREGISTER_ACTION);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EndpointDTO))
            return false;
        EndpointDTO other = (EndpointDTO) o;
        return getEndpointUuid().equals(other.getEndpointUuid());
    }

    @Override
    public int hashCode() {
        return getEndpointUuid().hashCode();
    }

    @Override
    public String toString() {
        return "EndpointDTO [endpointUuid=" + endpointUuid + ", action=" + action + ", endpointCallbackUrl="
                + endpointCallbackUrl + ", devicesUuid=" + devicesUuid + "]";
    }
}
