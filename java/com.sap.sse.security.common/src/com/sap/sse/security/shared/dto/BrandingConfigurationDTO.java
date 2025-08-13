package com.sap.sse.security.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BrandingConfigurationDTO implements IsSerializable {
    private boolean brandingActive;
    private String defaultBrandingLogoURL;
    private String greyTransparentLogoURL;
    
    // for GWT
    BrandingConfigurationDTO() {
        // Default constructor for GWT serialization
    }
    
    public BrandingConfigurationDTO(boolean brandingActive, String defaultBrandingLogoURL,
            String greyTransparentLogoURL) {
        this.brandingActive = brandingActive;
        this.defaultBrandingLogoURL = defaultBrandingLogoURL;
        this.greyTransparentLogoURL = greyTransparentLogoURL;
    }

    public boolean isBrandingActive() {
        return brandingActive;
    }

    public String getDefaultBrandingLogoURL() {
        return defaultBrandingLogoURL;
    }

    public String getGreyTransparentLogoURL() {
        return greyTransparentLogoURL;
    }
}
