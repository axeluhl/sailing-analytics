package com.sap.sse.util;

import java.io.Serializable;

/**
 * Implements {@link Object#equals(Object)} and {@link Object#hashCode()} based on the wrapped object's
 * identity, no matter what the object itself defines as {@code equals} and {@code hashCode}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class IdentityWrapper<T> implements Serializable {
    private static final long serialVersionUID = 3514488568026067341L;
    private final T t;
    
    public IdentityWrapper(T t) {
        this.t = t;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(t);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IdentityWrapper<?>) ? ((IdentityWrapper<?>) obj).t == this.t : obj == t;
    }

    @Override
    public String toString() {
        return "IdentityWrapper for "+t;
    }
}
