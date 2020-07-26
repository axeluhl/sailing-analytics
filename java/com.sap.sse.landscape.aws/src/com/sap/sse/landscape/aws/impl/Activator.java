package com.sap.sse.landscape.aws.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.persistence.PersistenceFactory;

public class Activator implements BundleActivator {
    private static Activator instance;
    
    private AwsLandscapeImpl<?, ApplicationProcessMetrics> landscape;

    @Override
    public void start(BundleContext context) throws Exception {
        instance = this;
        landscape = new AwsLandscapeImpl<>(System.getProperty(AwsLandscape.ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME),
                System.getProperty(AwsLandscape.SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME),
                PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory());
    }
    
    public static Activator getInstance() {
        return instance;
    }
    
    public AwsLandscapeImpl<?, ApplicationProcessMetrics> getLandscape() {
        return landscape;
    }

    public void setLandscape(AwsLandscapeImpl<?, ApplicationProcessMetrics> landscape) {
        this.landscape = landscape;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
