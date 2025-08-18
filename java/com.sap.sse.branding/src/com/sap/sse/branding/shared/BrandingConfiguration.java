package com.sap.sse.branding.shared;

import java.util.Optional;

public interface BrandingConfiguration {
    String getBrandTitle(Optional<String> locale);

    String getDefaultBrandingLogoURL(Optional<String> locale);

    String getGreyTransparentLogoURL(Optional<String> locale);
    
    String getSoutionsInSailingImageURL();
    
    String getSoutionsInSailingTrimmedImageURL();

    /**
     * The ID by which to find or set this configuration in {@code BrandingConfigurationService#setActiveBrandingConfigurationById(String)}
     */
    String getId();
}
