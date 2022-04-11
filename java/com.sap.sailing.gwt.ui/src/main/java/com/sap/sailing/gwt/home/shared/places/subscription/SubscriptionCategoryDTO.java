package com.sap.sailing.gwt.home.shared.places.subscription;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;

/**
 * A class to collect info from a bunch of SubscriptionPlanDTO which can be represented on one card.
 */
public class SubscriptionCategoryDTO implements IsSerializable {
    private static final long serialVersionUID = -3820239604039244557L;

    public static final String FEATURES_MESSAGE_KEY_SUFFX = "_features";
    public static final String NAME_MESSAGE_KEY_SUFFX = "_name";
    public static final String DESC_MESSAGE_KEY_SUFFX = "_description";
    public static final String INFO_MESSAGE_KEY_SUFFIX = "_info";
    public static final String PRICE_INFO_MESSAGE_KEY_SUFFIX = "_price_info";

    private final String id;
    private HashSet<SubscriptionPrice> prices = new HashSet<SubscriptionPrice>();
    private final Boolean isUserSubscribedToPlan;
    private final PlanCategory planCategory;
    private final Boolean userWasAlreadySubscribedToOneTimePlan;
    private final Boolean isUserSubscribedToPlanCategory;
    private final SubscriptionCard.Type type;

    public SubscriptionCategoryDTO(String id, boolean isUserSubscribedToPlan, Set<SubscriptionPrice> prices,
            PlanCategory planCategory, boolean userWasAlreadySubscribedToOneTimePlan,
            boolean isUserSubscribedToPlanCategory, String error, SubscriptionCard.Type type) {
        this.id = id;
        this.isUserSubscribedToPlan = isUserSubscribedToPlan;
        this.planCategory = planCategory;
        this.userWasAlreadySubscribedToOneTimePlan = userWasAlreadySubscribedToOneTimePlan;
        this.isUserSubscribedToPlanCategory = isUserSubscribedToPlanCategory;
        this.prices = new HashSet<SubscriptionPrice>(prices);
        this.type = type;
    }

    public PlanCategory getPlanCategory() {
        return planCategory;
    }

    public boolean isUserWasAlreadySubscribedToOneTimePlan() {
        return userWasAlreadySubscribedToOneTimePlan;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getSubscriptionCategoryId() {
        return id;
    }

    public boolean isUserSubscribedToPlan() {
        return isUserSubscribedToPlan;
    }

    public boolean isCurrentUserSubscribed() {
        return false;
    }

    public HashSet<SubscriptionPrice> getPrices() {
        return prices;
    }

    public boolean isUserSubscribedToPlanCategory() {
        return isUserSubscribedToPlanCategory;
    }

    public SubscriptionCard.Type getType() {
        return type;
    }

    public String getSubscriptionCategoryNameMessageKey() {
        return id + NAME_MESSAGE_KEY_SUFFX;
    }

    public String getSubscriptionCategoryDescMessageKey() {
        return id + DESC_MESSAGE_KEY_SUFFX;
    }

    public String getSubscriptionCategoryInfoMessageKey() {
        return id + INFO_MESSAGE_KEY_SUFFIX;
    }

    public String getSubscriptionCategoryFeatureMessageKey() {
        return id + FEATURES_MESSAGE_KEY_SUFFX;
    }

    public String getSubscriptionCategoryPriceInfoMessageKey() {
        return id + PRICE_INFO_MESSAGE_KEY_SUFFIX;
    }
}
