package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;

public class DeviceConfigurationMatcherDTO implements IsSerializable {
    
    public List<String> clients;
    public DeviceConfigurationMatcherType type;
    public int rank;
    
    public DeviceConfigurationMatcherDTO() {

    }
    
    public DeviceConfigurationMatcherDTO(DeviceConfigurationMatcherType type, List<String> clients, int rank) {
        this.clients = clients;
        this.type = type;
        this.rank = rank;
    }

}
