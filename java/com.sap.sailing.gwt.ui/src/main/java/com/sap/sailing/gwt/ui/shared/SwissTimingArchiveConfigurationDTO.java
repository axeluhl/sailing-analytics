package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingArchiveConfigurationDTO implements IsSerializable {
    private String jsonUrl;

    SwissTimingArchiveConfigurationDTO() {}
    
    public SwissTimingArchiveConfigurationDTO(String jsonUrl) {
        super();
        this.jsonUrl = jsonUrl;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }
}
