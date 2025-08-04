package com.sap.sse.branding.impl;

import com.sap.sse.branding.BrandingConfigurationService;

public class BrandingConfigurationServiceImpl implements BrandingConfigurationService {
    private boolean brandingActive;
    private String defaultBrandingLogoURL;
    private String greyTransparentLogoURL;

    public BrandingConfigurationServiceImpl() {
    }

    @Override
    public boolean isBrandingActive() {
        return brandingActive;
    }

    @Override
    public void setBrandingActive(boolean brandingActive) {
        this.brandingActive = brandingActive;
    }

    @Override
    public String getDefaultBrandingLogoURL() {
        return defaultBrandingLogoURL;
    }

    @Override
    public void setDefaultBrandingLogoURL(String defaultBrandingLogoURL) {
        this.defaultBrandingLogoURL = defaultBrandingLogoURL;
    }

    @Override
    public String getGreyTransparentLogoURL() {
        return greyTransparentLogoURL;
    }

    @Override
    public void setGreyTransparentLogoURL(String greyTransparentLogoURL) {
        this.greyTransparentLogoURL = greyTransparentLogoURL;
    }
}
