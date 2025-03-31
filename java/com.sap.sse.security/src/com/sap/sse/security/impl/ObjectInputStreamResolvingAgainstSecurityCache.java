package com.sap.sse.security.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class ObjectInputStreamResolvingAgainstSecurityCache extends ObjectInputStreamResolvingAgainstCache<UserStore> {
    ObjectInputStreamResolvingAgainstSecurityCache(InputStream in, UserStore cache, ResolveListener resolveListener, Map<String, Class<?>> classLoaderCache)
            throws IOException {
        super(in, cache, resolveListener, classLoaderCache);
    }
}
