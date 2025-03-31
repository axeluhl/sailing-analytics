package com.sap.sse.security.shared;

import java.io.Serializable;

/**
 * Equality and hash code are based on the {@link #getUserOwner()} and {@link #getTenantOwner()} results.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <G>
 * @param <U>
 */
public abstract class AbstractOwnership<G extends SecurityUserGroup<?>, U extends UserReference>
        implements Serializable {
    private static final long serialVersionUID = 1L;

    private U userOwner;
    private G tenantOwner;

    public AbstractOwnership(U userOwner, G tenantOwner) {
        this.userOwner = userOwner;
        this.tenantOwner = tenantOwner;
    }

    public U getUserOwner() {
        return userOwner;
    }

    public G getTenantOwner() {
        return tenantOwner;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tenantOwner == null) ? 0 : tenantOwner.hashCode());
        result = prime * result + ((userOwner == null) ? 0 : userOwner.hashCode());
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
        @SuppressWarnings("unchecked")
        AbstractOwnership<G, U> other = (AbstractOwnership<G, U>) obj;
        if (tenantOwner == null) {
            if (other.tenantOwner != null)
                return false;
        } else if (!tenantOwner.equals(other.tenantOwner))
            return false;
        if (userOwner == null) {
            if (other.userOwner != null)
                return false;
        } else if (!userOwner.equals(other.userOwner))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AbstractOwnership [userOwner=" + userOwner + ", tenantOwner=" + tenantOwner + "]";
    }
}
