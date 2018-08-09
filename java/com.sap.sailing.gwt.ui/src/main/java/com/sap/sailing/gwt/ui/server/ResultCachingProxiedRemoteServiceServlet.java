package com.sap.sailing.gwt.ui.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.sap.sailing.domain.common.dto.CachableRPCResult;
import com.sap.sse.common.Util.Pair;

public class ResultCachingProxiedRemoteServiceServlet extends DelegatingProxiedRemoteServiceServlet {

    private static final long serialVersionUID = -4245484615349695611L;
    
    /**
     * FIXME: This is not the final solution but just a prototype! It's not thread save and it does not free results
     * ever. Just good enough for local tests with a single user.
     */
    private Map<CacheKey, String> resultCache = new HashMap<>();
    
    @Override
    protected String encodeResponseForSuccess(Method serviceMethod, SerializationPolicy serializationPolicy, int flags,
            Object result) throws SerializationException {
        if (result instanceof CachableRPCResult) {
            final CachableRPCResult cacheableResult = (CachableRPCResult) result;
            try {
                return resultCache.computeIfAbsent(new CacheKey(serializationPolicy, cacheableResult),
                        key -> {
                            try {
                                return super.encodeResponseForSuccess(serviceMethod, serializationPolicy, flags, result);
                            } catch (SerializationException serializationException) {
                                throw new TemporaryWrapperException(serializationException);
                            }
                        });
            } catch (TemporaryWrapperException e) {
                throw e.serializationException;
            }
        }
        return super.encodeResponseForSuccess(serviceMethod, serializationPolicy, flags, result);
    }
    
    /**
     * We can't just use {@link Pair} because we check for object identity of the cacheableResults instead of calling
     * equals.
     */
    private class CacheKey {
        private final SerializationPolicy serializationPolicy;
        private final CachableRPCResult cacheableResult;

        public CacheKey(SerializationPolicy serializationPolicy, CachableRPCResult cacheableResult) {
            this.serializationPolicy = serializationPolicy;
            this.cacheableResult = cacheableResult;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((cacheableResult == null) ? 0 : cacheableResult.hashCode());
            result = prime * result + ((serializationPolicy == null) ? 0 : serializationPolicy.hashCode());
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
            CacheKey other = (CacheKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (cacheableResult == null) {
                if (other.cacheableResult != null)
                    return false;
            } else if (cacheableResult != other.cacheableResult)
                return false;
            if (serializationPolicy == null) {
                if (other.serializationPolicy != null)
                    return false;
            } else if (!serializationPolicy.equals(other.serializationPolicy))
                return false;
            return true;
        }

        private ResultCachingProxiedRemoteServiceServlet getOuterType() {
            return ResultCachingProxiedRemoteServiceServlet.this;
        }
    }

    private class TemporaryWrapperException extends RuntimeException {
        private static final long serialVersionUID = 4720608117776979002L;
        private final SerializationException serializationException;

        public TemporaryWrapperException(SerializationException serializationException) {
            this.serializationException = serializationException;
        }
    }
}
