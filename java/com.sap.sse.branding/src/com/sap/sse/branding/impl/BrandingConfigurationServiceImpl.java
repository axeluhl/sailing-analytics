package com.sap.sse.branding.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.branding.BrandingConfiguration;
import com.sap.sse.branding.BrandingConfigurationService;
import com.sap.sse.util.ServiceTrackerFactory;

public class BrandingConfigurationServiceImpl implements BrandingConfigurationService {
    private ServiceTracker<BrandingConfiguration, BrandingConfiguration> brandingConfigurationTracker;
    
    private final BundleContext bundleContext;
    
    public BrandingConfigurationServiceImpl(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
    }

    @Override
    public boolean isBrandingActive() {
        return brandingConfigurationTracker != null && brandingConfigurationTracker.getService() != null;
    }

    @Override
    public BrandingConfiguration setActiveBrandingConfigurationById(String brandingConfigurationId) {
        try {
            Filter filter = bundleContext.createFilter(
                    String.format("(&(%s=%s)(%s=%s))",
                            BrandingConfigurationService.BRANDING_ID_PROPERTY_NAME, ""+brandingConfigurationId,
                            Constants.OBJECTCLASS, BrandingConfiguration.class.getName()));
            brandingConfigurationTracker = ServiceTrackerFactory.createAndOpen(bundleContext, filter);
            return brandingConfigurationTracker.getService();
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Internal error: Invalid filter syntax for BrandingConfigurationService", e);
        }
    }

    @Override
    public BrandingConfiguration getActiveBrandingConfiguration() {
        return brandingConfigurationTracker == null ? null : brandingConfigurationTracker.getService();
    }
}
