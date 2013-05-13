package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.ObjectInputStreamResolvingAgainstDomainFactory;

public class ObjectInputStreamResolvingAgainstDomainFactoryImpl extends ObjectInputStreamResolvingAgainstDomainFactory {
    ObjectInputStreamResolvingAgainstDomainFactoryImpl(InputStream in, DomainFactory domainFactory)
            throws IOException {
        super(in, domainFactory);
    }
}
