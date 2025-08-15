package com.sap.sse.branding.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.json.simple.JSONObject;
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
    public Map<BrandingConfigurationProperty, Object> getBrandingConfigurationPropertiesForJspContext(Optional<String> locale) {
        final BrandingConfiguration brandingConfiguration = getActiveBrandingConfiguration();
        final Map<BrandingConfigurationProperty, Object> map = new HashMap<>();
        final String title;
        final String whitelabeled;
        if (brandingConfiguration != null) {
            title = brandingConfiguration.getBrandTitle(locale)+" ";
            whitelabeled = "";
        } else {
            title = "";
            whitelabeled = "-whitelabeled";
        }
        // TODO bug6060: figure out how to deal with i18n and the locale parameter
        map.put(BrandingConfigurationProperty.BRAND_TITLE_WITH_TRAILING_SPACE_JSP_PROPERTY_NAME, title);
        map.put(BrandingConfigurationProperty.DEBRANDING_ACTIVE_JSP_PROPERTY_NAME, !isBrandingActive());
        map.put(BrandingConfigurationProperty.BRANDING_ACTIVE_JSP_PROPERTY_NAME, isBrandingActive());
        map.put(BrandingConfigurationProperty.DASH_WHITELABELED_JSP_PROPERTY_NAME, whitelabeled);
        map.put(BrandingConfigurationProperty.SCRIPT_FOR_CLIENT_CONFIGURATION_CONTEXT_TO_DOCUMENT_JSP_PROPERTY_NAME, generateScriptForClientConfigurationContext(map));
        return map;
    }

    private Object generateScriptForClientConfigurationContext(Map<BrandingConfigurationProperty, Object> map) {
        final StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append("document.clientConfigurationContext=");
        final JSONObject jsonObject = new JSONObject();
        for (final Entry<BrandingConfigurationProperty, Object> brandingConfigurationPropertyAndValue : map.entrySet()) {
            jsonObject.put(brandingConfigurationPropertyAndValue.getKey().getPropertyName(), brandingConfigurationPropertyAndValue.getValue());
        }
        scriptBuilder.append(jsonObject.toJSONString());
        scriptBuilder.append(";");
        return scriptBuilder.toString();
    }
}
