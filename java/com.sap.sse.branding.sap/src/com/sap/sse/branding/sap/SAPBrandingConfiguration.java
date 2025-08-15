package com.sap.sse.branding.sap;

import java.util.Optional;

import com.sap.sse.branding.shared.BrandingConfiguration;

public class SAPBrandingConfiguration implements BrandingConfiguration {
    public static final String ID = "SAP";
    private String defaultBrandingLogoURL;
    private String greyTransparentLogoURL;

    public SAPBrandingConfiguration() {
    }

    @Override
    public String getDefaultBrandingLogoURL(Optional<String> locale) {
        return defaultBrandingLogoURL;
    }

    @Override
    public String getGreyTransparentLogoURL(Optional<String> locale) {
        return greyTransparentLogoURL;
    }

    @Override
    public String getBrandTitle(Optional<String> locale) {
        return "SAP";
    }

    @Override
    public String getId() {
        return ID;
    }
}
