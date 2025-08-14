package com.sap.sse.branding.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.branding.BrandingConfigurationService;
import com.sap.sse.branding.shared.BrandingConfiguration;
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


    @Override
    public Map<BrandingConfigurationProperty, Object> getBrandingConfigurationPropertiesForJspContext() {
        final Map<BrandingConfigurationProperty, Object> map = new HashMap<>();
        final String title;
        final String whitelabeled;
        if (isBrandingActive()) {
            title = "SAP ";
            whitelabeled = "";
        } else {
            title = "";
            whitelabeled = "-whitelabeled";
        }
        map.put(BrandingConfigurationProperty.BRAND_TITLE_WITH_TRAILING_SPACE_JSP_PROPERTY_NAME, title);
        map.put(BrandingConfigurationProperty.DEBRANDING_ACTIVE_JSP_PROPERTY_NAME, !isBrandingActive());
        map.put(BrandingConfigurationProperty.BRANDING_ACTIVE_JSP_PROPERTY_NAME, isBrandingActive());
        map.put(BrandingConfigurationProperty.DASH_WHITELABELED_JSP_PROPERTY_NAME, whitelabeled);
        return map;
    }
}
