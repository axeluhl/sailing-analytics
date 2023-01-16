package com.sap.sse.security.ui.client.premium.uielements;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface PremiumToogleButtonResource extends ClientBundle {

    public static final PremiumToogleButtonResource INSTANCE = GWT.create(PremiumToogleButtonResource.class);

    interface PremiumToogleButtonStyle extends CssResource {
        @ClassName("premium-container")
        String premiumContainer();

        @ClassName("premium-button")
        String premiumButton();

        @ClassName("premium-permitted")
        String premiumPermitted();

        @ClassName("not-premium-permitted")
        String notPremiumPermitted();

        @ClassName("premium-active")
        String premiumActive();
    }

    @Source("PremiumToggleButton.gss")
    PremiumToogleButtonStyle css();

    @Source("icon_premium.svg")
    @MimeType("image/svg+xml")
    DataResource premiumIcon();

}
