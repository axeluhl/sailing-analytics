package com.sap.sse.common.impl;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;

public class NamedWithUUIDImpl extends NamedImpl implements NamedWithID {
    private static final long serialVersionUID = -3423929856042951606L;
    private final UUID id;
    
    public NamedWithUUIDImpl(String name) {
        this(name, UUID.randomUUID());
    }
    
    public NamedWithUUIDImpl(String name, UUID id) {
        super(name);
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
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
        NamedWithUUIDImpl other = (NamedWithUUIDImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
