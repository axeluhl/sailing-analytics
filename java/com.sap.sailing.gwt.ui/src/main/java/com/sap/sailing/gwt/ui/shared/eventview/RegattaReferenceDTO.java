package com.sap.sailing.gwt.ui.shared.eventview;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaReferenceDTO implements IsSerializable {
    private String name;
    
    public RegattaReferenceDTO() {
    }
    
    public RegattaReferenceDTO(String name) {
        super();
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
