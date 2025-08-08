package com.sap.sse.branding.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.branding.BrandingConfigurationService;

public class Activator implements BundleActivator {
    private static BrandingConfigurationService defaultBrandingConfigurationService;

    public static BrandingConfigurationService getDefaultBrandingConfigurationService() {
        return defaultBrandingConfigurationService;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        defaultBrandingConfigurationService = new BrandingConfigurationServiceImpl();
        final boolean debrandingActive = Boolean.valueOf(System.getProperty(BrandingConfigurationService.DEBRANDING_PROPERTY_NAME, "true"));
        defaultBrandingConfigurationService.setBrandingActive(!debrandingActive);
        context.registerService(BrandingConfigurationService.class.getName(), defaultBrandingConfigurationService, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
