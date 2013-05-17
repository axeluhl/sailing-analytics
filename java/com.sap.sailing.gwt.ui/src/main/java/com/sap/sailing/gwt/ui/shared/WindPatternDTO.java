package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.dto.NamedDTO;

public class WindPatternDTO extends NamedDTO {
    private static final long serialVersionUID = 2765611781878430790L;
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
