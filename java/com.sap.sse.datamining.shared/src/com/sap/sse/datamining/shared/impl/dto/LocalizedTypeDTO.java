package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

public class LocalizedTypeDTO implements Serializable {
    private static final long serialVersionUID = -5976605483497225403L;
    
    private String typeName;
    private String displayName;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    LocalizedTypeDTO() { }
    
    public LocalizedTypeDTO(String typeName, String displayName) {
        this.typeName = typeName;
        this.displayName = displayName;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return getTypeName() + "[" + getDisplayName() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalizedTypeDTO other = (LocalizedTypeDTO) obj;
        if (displayName == null) {
            if (other.displayName != null)
                return false;
        } else if (!displayName.equals(other.displayName))
            return false;
        if (typeName == null) {
            if (other.typeName != null)
                return false;
        } else if (!typeName.equals(other.typeName))
            return false;
        return true;
    }

}
