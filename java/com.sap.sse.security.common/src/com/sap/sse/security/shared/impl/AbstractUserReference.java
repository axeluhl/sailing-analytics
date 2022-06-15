package com.sap.sse.security.shared.impl;

import java.io.Serializable;

import com.sap.sse.security.shared.UserReference;

/**
 * Equality and hash code is based only on the user {@link #getName() name}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractUserReference implements UserReference {
    private static final long serialVersionUID = -3639860207453072248L;

    private String name;
    
    public AbstractUserReference(String name) {
        this.name = name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        AbstractUserReference other = (AbstractUserReference) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Serializable getId() {
        return getName();
    }

    @Override
    public String toString() {
        return name;
    }
}
