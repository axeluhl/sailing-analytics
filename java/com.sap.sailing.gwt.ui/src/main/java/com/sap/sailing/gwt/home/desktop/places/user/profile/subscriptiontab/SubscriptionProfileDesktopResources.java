package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Tu Tran
 */
public interface SubscriptionProfileDesktopResources extends ClientBundle {
    public static final SubscriptionProfileDesktopResources INSTANCE = GWT
            .create(SubscriptionProfileDesktopResources.class);

    @Source("SubscriptionProfiles.gss")
    SubscriptionProfileCss css();

    public interface SubscriptionProfileCss extends CssResource {
        String emptySubscriptionsLabel();
        
        String bottomButton();
        
        String sectionLabel();

        String plansInput();

        String planInputContainer();
    }
}
