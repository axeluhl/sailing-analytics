package com.sap.sse.datamining.shared.impl;

import java.util.UUID;

public class UUIDDataMiningSession extends AbstractDataMiningSession {

    private static final long serialVersionUID = 7794343618067812057L;
    
    private UUID id;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    public UUIDDataMiningSession() { }

    public UUIDDataMiningSession(UUID id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        UUIDDataMiningSession other = (UUIDDataMiningSession) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
