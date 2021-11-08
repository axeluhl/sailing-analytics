package com.sap.sse.gwt.server;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.sap.sse.common.CacheableRPCResult;

/**
 * A weak reference to a {@link CacheableRPCResult} object, augmented by a reference to the
 * {@link SerializationPolicy} that is to be used to serialize the result as a response of
 * a GWT RPC call.<p>
 * 
 * The object computes its hash code at construction and remembers it. This way, even if the
 * weak reference is {@link #clear() cleared}, the hash code remains constant. Equality is
 * decided based on the equality of the {@link SerializationPolicy} and the <em>identity</em> of
 * the {@link CacheableRPCResult} object.<p>
 * 
 * With this approach, an object of this type will always be {@link #equals(Object) equal} to itself,
 * and its hash code will remain constant, thus guaranteeing that removing it from a hashed data
 * structure such as a {@link HashMap} will always work, even if the weak reference was cleared
 * since insertion.<p>
 * 
 * Note: the case of a cleared weak reference that is compared to a new {@link CacheKey} constructed
 * with the <em>same</em> original {@link CacheableRPCResult} does not need to be considered because
 * clearing the weak reference implies the unreachability of the original {@link CacheableRPCResult}.
 */
public class CacheKey extends WeakReference<CacheableRPCResult> {
    private final SerializationPolicy serializationPolicy;
    private final int hashCode;

    public CacheKey(SerializationPolicy serializationPolicy, CacheableRPCResult cacheableResult, ReferenceQueue<CacheableRPCResult> dereferencedObjectsQueue) {
        super(cacheableResult, dereferencedObjectsQueue);
        this.serializationPolicy = serializationPolicy;
        this.hashCode = internalHashCode();
    }

    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((get() == null) ? 0 : get().hashCode());
        result = prime * result + ((serializationPolicy == null) ? 0 : serializationPolicy.hashCode());
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CacheKey other = (CacheKey) obj;
        if (get() != other.get()) // compare identity, not equality
            return false;
        if (serializationPolicy == null) {
            if (other.serializationPolicy != null)
                return false;
        } else if (!serializationPolicy.equals(other.serializationPolicy))
            return false;
        return true;
    }
}