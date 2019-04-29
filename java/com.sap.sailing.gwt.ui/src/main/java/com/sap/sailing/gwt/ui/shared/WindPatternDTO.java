package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.gwt.ui.simulator.windpattern.WindPattern;
import com.sap.sse.security.shared.dto.NamedDTO;

public class WindPatternDTO extends NamedDTO {

    private static final long serialVersionUID = 7533083343024427310L;

    private WindPattern pattern;
    
    public WindPatternDTO() {
    }
    
    public WindPatternDTO(WindPattern pattern) {
        super(pattern.name());
        this.pattern = pattern;
    }
    
    public WindPattern getPattern() {
        return pattern;
    }
    
}
