package com.sap.sailing.gwt.home.desktop.places.user.profile.subscriptionstab;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileResources;

/**
 * @author Tu Tran
 */
public interface UserProfileSubscriptionsResources extends ClientBundle {
    public static final UserProfileSubscriptionsResources INSTANCE = GWT
            .create(UserProfileSubscriptionsResources.class);

    @Source({ "../UserProfile.gss", "UserProfileSubscriptions.gss" })
    SubscriptionProfileCss css();

    public interface SubscriptionProfileCss extends UserProfileResources.UserProfileStyle {

        String fontWeightBold();

        String fontStyleItalic();

        String textAlignRight();

        String textColorBlue();

        String textColorRed();

        String defaultTableRow();

        String borderTableRow();
    }
}
