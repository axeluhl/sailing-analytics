package com.sap.sse.branding.shared;

public interface BrandingConfiguration {
    String getBrandTitle();

    String getDefaultBrandingLogoURL();

    String getGreyTransparentLogoURL();

    /**
     * The ID by which to find or set this configuration in {@code BrandingConfigurationService#setActiveBrandingConfigurationById(String)}
     */
    String getId();
}
