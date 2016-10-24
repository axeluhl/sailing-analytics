package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DeviceConfigurationMatcherDTO implements IsSerializable {
    public List<String> clients;
    
    public DeviceConfigurationMatcherDTO() {

    }
    
    public DeviceConfigurationMatcherDTO(List<String> clients) {
        this.clients = clients;
    }
}
