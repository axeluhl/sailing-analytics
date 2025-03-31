package com.sap.sse.security.ui.shared.subscription;

public interface HasSubscriptionMessageKeys{
    public static final String FEATURES_MESSAGE_KEY_SUFFX = "_features";
    public static final String NAME_MESSAGE_KEY_SUFFX = "_name";
    public static final String DESC_MESSAGE_KEY_SUFFX = "_description";
    public static final String INFO_MESSAGE_KEY_SUFFIX = "_info";
    public static final String PRICE_INFO_MESSAGE_KEY_SUFFIX = "_price_info";
    
    String getSubscriptionPlanId();
    
    default public String getSubscriptionPlanNameMessageKey() {
        final String subscriptionPlanId = getSubscriptionPlanId();
        return subscriptionPlanId.substring(subscriptionPlanId.indexOf("_") + 1) + NAME_MESSAGE_KEY_SUFFX;
    }

    default public String getSubscriptionPlanDescMessageKey() {
        return getSubscriptionPlanId() + DESC_MESSAGE_KEY_SUFFX;
    }

    default public String getSubscriptionPlanInfoMessageKey() {
        return getSubscriptionPlanId() + INFO_MESSAGE_KEY_SUFFIX;
    }

    default public String getSubscriptionPlanFeatureMessageKey() {
        return getSubscriptionPlanId() + FEATURES_MESSAGE_KEY_SUFFX;
    }

    default public String getSubscriptionPlanPriceInfoMessageKey() {
        return getSubscriptionPlanId() + PRICE_INFO_MESSAGE_KEY_SUFFIX;
    }
}
