package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

/**
 * Equality and hash code are defined based on Java object identity
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class ControlPointDTO extends NamedDTO {
    private Serializable id;
    
    public ControlPointDTO() {}
    
    public ControlPointDTO(Serializable id, String name) {
        super(name);
        this.id = id;
    }
    
    public Serializable getId() {
        return id;
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
