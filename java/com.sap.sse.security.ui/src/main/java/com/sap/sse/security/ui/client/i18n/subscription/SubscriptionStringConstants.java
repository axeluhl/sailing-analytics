package com.sap.sse.security.ui.client.i18n.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface SubscriptionStringConstants extends ConstantsWithLookup {
    public static final SubscriptionStringConstants INSTANCE = GWT.create(SubscriptionStringConstants.class);
    
    String pro_plan_name();
    String pro_plan_description();
}
