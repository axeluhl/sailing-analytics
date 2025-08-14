package com.sap.sse.branding.sap;

import com.sap.sse.branding.shared.BrandingConfiguration;

public class SAPBrandingConfiguration implements BrandingConfiguration {
    public static final String ID = "SAP";
    private String defaultBrandingLogoURL;
    private String greyTransparentLogoURL;

    public SAPBrandingConfiguration() {
    }

    @Override
    public String getDefaultBrandingLogoURL() {
        return defaultBrandingLogoURL;
    }

    @Override
    public String getGreyTransparentLogoURL() {
        return greyTransparentLogoURL;
    }

    @Override
    public String getBrandTitle() {
        return "SAP";
    }

    @Override
    public String getId() {
        return ID;
    }
}
