package com.sap.sse.mail.impl;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.mail.MailService;
import com.sap.sse.mail.MailServiceResolver;

public class MailServiceResolverAgainstOsgiRegistryImpl implements MailServiceResolver {
    private final TypeBasedServiceFinder<MailService> serviceFinder;

    public MailServiceResolverAgainstOsgiRegistryImpl(TypeBasedServiceFinder<MailService> serviceFinder) {
        this.serviceFinder = serviceFinder;
    }

    @Override
    public MailService getMailService() {
        try {
            return serviceFinder.findService(null);
        } catch (NoCorrespondingServiceRegisteredException e) {
            return null;
        }
    }

}
