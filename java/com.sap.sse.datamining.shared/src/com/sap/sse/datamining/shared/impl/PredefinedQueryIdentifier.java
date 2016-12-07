package com.sap.sse.datamining.shared.impl;

import java.io.Serializable;

public class PredefinedQueryIdentifier implements Serializable {
    private static final long serialVersionUID = 7070218053513954406L;
    
    private String identifier;
    private String description;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    PredefinedQueryIdentifier() { }
    
    public PredefinedQueryIdentifier(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
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
        PredefinedQueryIdentifier other = (PredefinedQueryIdentifier) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        return true;
    }

}