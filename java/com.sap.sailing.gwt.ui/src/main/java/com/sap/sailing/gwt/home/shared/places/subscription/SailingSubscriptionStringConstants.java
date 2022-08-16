package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.sap.sse.security.ui.client.i18n.subscription.SubscriptionStringConstants;

@DefaultLocale("en")
public interface SailingSubscriptionStringConstants extends SubscriptionStringConstants {
    public static final SailingSubscriptionStringConstants INSTANCE = GWT.create(SailingSubscriptionStringConstants.class);
}
