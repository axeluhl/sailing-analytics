package com.sap.sse.security.ui.client.i18n.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface SubscriptionStringConstants extends ConstantsWithLookup {
    public static final SubscriptionStringConstants INSTANCE = GWT.create(SubscriptionStringConstants.class);

    String payment_interval_YEAR();
    String payment_interval_MONTH();
    String payment_interval_WEEK();
    String payment_interval_DAY();
    String support_subject();
    
    String businessModelTitle();
    String businessModelDescription();
    String free_subscription_plan_name();
    String free_subscription_plan_description();
    String free_subscription_plan_info();
    String free_subscription_plan_price_info();
    String[] free_subscription_plan_features();
    String yearly_premium_name();
    String yearly_premium_description();
    String yearly_premium_price_info();
    String yearly_premium_info();
    String[] yearly_premium_features();
    String weekly_premium_name();
    String weekly_premium_description();
    String weekly_premium_price_info();
    String weekly_premium_info();
    String[] weekly_premium_features();
    String trial_premium_name();
    String trial_premium_description();
    String trial_premium_price_info();
    String trial_premium_info();
    String[] trial_premium_features();
    String free_subscription_plan_shortname();
    String premium_subscription_plan_shortname();
    String datamining_subscription_plan_shortname();
    String features_map_analytics_title();
    String features_map_analytics_description();
    String features_simulator_title();
    String features_simulator_description();
    String features_advanced_leaderboard_info_title();
    String features_advanced_leaderboard_info_description();
    String features_competitor_analytics_title();
    String features_competitor_analytics_description();
    String features_maneuver_analytics_title();
    String features_maneuver_analytics_description();
    String features_wind_analytics_title();
    String features_wind_analytics_description();
    String features_limited_live_analytics_title();
    String features_limited_live_analytics_description();
    String features_full_live_analytics_title();
    String features_full_live_analytics_description();
    String features_organize_events_title();
    String features_organize_events_description();
    String features_events_with_more_regatta_title();
    String features_events_with_more_regatta_description();
    String features_connect_to_tractrac_title();
    String features_connect_to_tractrac_description();
    String features_imports_title();
    String features_imports_description();
    String features_media_management_title();
    String features_media_management_description();
    String features_analytic_charts_title();
    String features_analytic_charts_description();
    String features_media_tags_title();
    String features_media_tags_description();
    String features_scoring_title();
    String features_scoring_description();
}
