package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptiontab;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SubscriptionProfileDesktopResources extends ClientBundle {
    public static final SubscriptionProfileDesktopResources INSTANCE = GWT
            .create(SubscriptionProfileDesktopResources.class);

    @Source("SubscriptionProfiles.gss")
    SubscriptionProfileCss css();

    public interface SubscriptionProfileCss extends CssResource {
        String bottomButton();

        String trialText();

        String textRow();

        String errorText();

        String blueText();

        String plansLabel();

        String plansInput();

        String planInputContainer();
    }
}
