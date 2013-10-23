package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DeviceConfigurationMatcherDTO implements IsSerializable {
    
    public enum Type {
        UNKNOWN,
        SINGLE,
        MULTI,
        ANY
    }
    
    public List<String> clients;
    public Type type;
    public int rank;
    
    public DeviceConfigurationMatcherDTO() {

    }
    
    public DeviceConfigurationMatcherDTO(Type type, List<String> clients, int rank) {
        this.clients = clients;
        this.type = type;
        this.rank = rank;
    }

}
