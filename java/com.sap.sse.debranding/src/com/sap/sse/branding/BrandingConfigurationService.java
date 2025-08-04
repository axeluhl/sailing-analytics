package com.sap.sse.branding;

public interface BrandingConfigurationService {
    String DEBRANDING_PROPERTY_NAME = "com.sap.sse.debranding";
    
    boolean isBrandingActive();
    String getDefaultBrandingLogoURL();
    String getGreyTransparentLogoURL();
    void setBrandingActive(boolean brandingActive);
    void setDefaultBrandingLogoURL(String defaultBrandingLogoURL);
    void setGreyTransparentLogoURL(String greyTransparentLogoURL);
}
