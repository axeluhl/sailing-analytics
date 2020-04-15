package com.sap.sailing.gwt.ui.shared;

import com.sap.sse.security.shared.dto.NamedDTO;


/**
 * Equality and hash code are defined based on Java object identity
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class ControlPointDTO extends NamedDTO {
    private static final long serialVersionUID = 2321067329143412902L;
    private String idAsString;
    private String shortName;
    
    public ControlPointDTO() {}
    
    public ControlPointDTO(String idAsString, String name, String shortName) {
        super(name);
        this.idAsString = idAsString;
        this.shortName = shortName;
    }
    
    public String getIdAsString() {
        return idAsString;
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    public abstract Iterable<MarkDTO> getMarks();
}
