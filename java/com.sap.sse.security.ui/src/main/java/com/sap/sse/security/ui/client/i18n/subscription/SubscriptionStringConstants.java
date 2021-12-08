package com.sap.sse.security.ui.client.i18n.subscription;

import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.sap.sse.security.shared.StringMessagesKey;

@DefaultLocale("en")
public interface SubscriptionStringConstants extends ConstantsWithLookup {
    public static final SubscriptionStringConstants INSTANCE = GWT.create(SubscriptionStringConstants.class);

    default String getString(final StringMessagesKey messageKey) {
        try {
            return getString(messageKey.getKey());
        } catch (MissingResourceException e) {
            GWT.log("Cannot find resource with key '" + messageKey.getKey() + "'!");
            return "???" + messageKey.getKey() + "???";
        }
    }

    String free_subscription_plan_name();
    String free_subscription_plan_description();
    String free_feature_1();
    String free_feature_2();
    String free_feature_3();
    String free_feature_4();
    String payment_interval_YEAR();
    String payment_interval_MONTH();
    String payment_interval_WEEK();
    String payment_interval_DAY();
    String individual_subscription_plan_name();
    String individual_subscription_plan_description();
    String basic_plan_name();
    String basic_plan_desc();
    String advanced_plan_name();
    String advanced_plan_desc();
    String aaas_plan_name();
    String aaas_plan_desc();
}
