package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindPatternDTO extends NamedDTO implements IsSerializable {

    private String displayName;
    
    public WindPatternDTO() {
    }
    
    
    public WindPatternDTO(String name, String displayName) {
      super(name);
      this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
