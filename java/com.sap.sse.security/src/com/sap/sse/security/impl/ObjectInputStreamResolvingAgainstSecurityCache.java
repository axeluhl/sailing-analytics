package com.sap.sse.security.impl;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sse.security.UserStore;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class ObjectInputStreamResolvingAgainstSecurityCache extends ObjectInputStreamResolvingAgainstCache<UserStore> {
    ObjectInputStreamResolvingAgainstSecurityCache(InputStream in, UserStore cache, ResolveListener resolveListener)
            throws IOException {
        super(in, cache, resolveListener);
    }
}
