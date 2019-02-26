package com.sap.sse.gwt.server;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.sap.sse.common.CacheableRPCResult;
import com.sap.sse.common.Util.Pair;

/**
 * We can't just use {@link Pair} because we check for object identity of the {@link #cacheableResult}s instead of
 * calling equals. Uses a {@link WeakReference} to the {@code cacheableResult} and remembers the
 * {@code cacheableResult}'s hash code at the time of object creation. If the reference is cleared and enqueued in
 * {@link ResultCachingProxiedRemoteServiceServlet#dereferencedObjectsQueue} later, this object will be retrieved
 * using its constant hash code, where equality is decided based on the weak reference's referent which after
 * enqueuing is {@code null}, hence allowing a {@link Map#remove(Object)} operation to succeed.
 */
public class CacheKey {
    private final SerializationPolicy serializationPolicy;
    final WeakReference<CacheableRPCResult> cacheableResult; // package-protected to let test fragment see it
    private final int hashCode;

    public CacheKey(SerializationPolicy serializationPolicy, CacheableRPCResult cacheableResult, ReferenceQueue<CacheableRPCResult> dereferencedObjectsQueue) {
        this.serializationPolicy = serializationPolicy;
        this.cacheableResult = new WeakReference<>(cacheableResult, dereferencedObjectsQueue);
        this.hashCode = internalHashCode();
    }

    private int internalHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cacheableResult.get() == null) ? 0 : cacheableResult.get().hashCode());
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
        if (cacheableResult.get() != other.cacheableResult.get()) // compare identity, not equality
            return false;
        if (serializationPolicy == null) {
            if (other.serializationPolicy != null)
                return false;
        } else if (!serializationPolicy.equals(other.serializationPolicy))
            return false;
        return true;
    }
}