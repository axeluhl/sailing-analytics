package com.sap.sse.security.shared.dto;

import java.io.Serializable;

public class BrandingConfigurationDTO implements Serializable {
    private static final long serialVersionUID = -8823261111571006856L;
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
