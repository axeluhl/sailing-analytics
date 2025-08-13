package com.sap.sse.branding;

public interface BrandingConfiguration {
    String getBrandTitle();

    String getDefaultBrandingLogoURL();

    String getGreyTransparentLogoURL();

    /**
     * The ID by which to find or set this configuration in {@link BrandingConfigurationService#setActiveBrandingConfigurationById(String)}
     */
    String getId();
}
