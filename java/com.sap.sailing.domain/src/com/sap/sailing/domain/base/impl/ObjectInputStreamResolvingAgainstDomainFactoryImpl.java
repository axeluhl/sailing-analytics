package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class ObjectInputStreamResolvingAgainstDomainFactoryImpl extends ObjectInputStreamResolvingAgainstCache<DomainFactory> {
    ObjectInputStreamResolvingAgainstDomainFactoryImpl(InputStream in, DomainFactory domainFactory,
            ResolveListener resolveListener, Map<String, Class<?>> classLoaderCache) throws IOException {
        super(in, domainFactory, resolveListener, classLoaderCache);
    }
}
