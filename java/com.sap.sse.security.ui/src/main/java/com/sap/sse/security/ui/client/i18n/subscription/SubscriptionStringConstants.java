package com.sap.sse.security.ui.client.i18n.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface SubscriptionStringConstants extends ConstantsWithLookup {
    public static final SubscriptionStringConstants INSTANCE = GWT.create(SubscriptionStringConstants.class);

    String free_subscription_plan_name();
    String free_subscription_plan_description();
    String[] free_subscription_plan_features();
    String payment_interval_YEAR();
    String payment_interval_MONTH();
    String payment_interval_WEEK();
    String payment_interval_DAY();
    String individual_subscription_plan_name();
    String individual_subscription_plan_description();
    String[] individual_subscription_plan_features();
    String yearly_premium_name();
    String yearly_premium_description();
    String[] yearly_premium_features();
    String weekly_premium_name();
    String weekly_premium_description();
    String[] weekly_premium_features();
}
