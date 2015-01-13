package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;

public class ClusterDTO implements Serializable {
    private static final long serialVersionUID = -2962035066215989018L;
    
    private String signifier;

    /**
     * <b>Constructor for the GWT-Serialization. Don't use this!</b>
     */
    @Deprecated
    ClusterDTO() { }

    public ClusterDTO(String signifier) {
        this.signifier = signifier;
    }
    
    public String getSignifier() {
        return signifier;
    }
    
    @Override
    public String toString() {
        return getSignifier();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((signifier == null) ? 0 : signifier.hashCode());
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
        ClusterDTO other = (ClusterDTO) obj;
        if (signifier == null) {
            if (other.signifier != null)
                return false;
        } else if (!signifier.equals(other.signifier))
            return false;
        return true;
    }
    
}
