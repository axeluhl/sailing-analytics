package com.sap.sse.security.impl;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sse.replication.impl.ObjectInputStreamResolvingAgainstCache;
import com.sap.sse.security.Cache;

public class ObjectInputStreamResolvingAgainstSecurityCache extends ObjectInputStreamResolvingAgainstCache<Cache> {
    ObjectInputStreamResolvingAgainstSecurityCache(InputStream in, Cache cache) throws IOException {
        super(in, cache);
    }
}
