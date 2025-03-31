package com.sap.sailing.gwt.home.shared.places.subscription;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanGroup;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;

/**
 * A class to collect info from a bunch of SubscriptionPlanDTO which can be represented on one card.
 */
public class SubscriptionGroupDTO implements IsSerializable {
    private static final long serialVersionUID = -3820239604039244557L;

    public static final String FEATURES_MESSAGE_KEY_SUFFX = "_features";
    public static final String NAME_MESSAGE_KEY_SUFFX = "_name";
    public static final String DESC_MESSAGE_KEY_SUFFX = "_description";
    public static final String INFO_MESSAGE_KEY_SUFFIX = "_info";
    public static final String PRICE_INFO_MESSAGE_KEY_SUFFIX = "_price_info";

    private final String id;
    private HashSet<SubscriptionPrice> prices = new HashSet<SubscriptionPrice>();
    private final Boolean isUserSubscribedToPlan;
    private final PlanGroup group;
    private final Boolean isUserSubscribedToGroup;
    private final SubscriptionCard.Type type;

    public SubscriptionGroupDTO(String id, boolean isUserSubscribedToPlan, Set<SubscriptionPrice> prices,
            PlanGroup group, boolean isUserSubscribedToGroup, String error, SubscriptionCard.Type type) {
        this.id = id;
        this.isUserSubscribedToPlan = isUserSubscribedToPlan;
        this.group = group;
        this.isUserSubscribedToGroup = isUserSubscribedToGroup;
        this.prices = new HashSet<SubscriptionPrice>(prices);
        this.type = type;
    }

    public PlanGroup getGroup() {
        return group;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getSubscriptionGroupId() {
        return id;
    }

    public boolean isUserSubscribedToPlan() {
        return isUserSubscribedToPlan;
    }

    public HashSet<SubscriptionPrice> getPrices() {
        return prices;
    }

    public boolean isUserSubscribedToGroup() {
        return isUserSubscribedToGroup;
    }

    public SubscriptionCard.Type getType() {
        return type;
    }

    public String getSubscriptionGroupNameMessageKey() {
        return id + NAME_MESSAGE_KEY_SUFFX;
    }

    public String getSubscriptionGroupDescMessageKey() {
        return id + DESC_MESSAGE_KEY_SUFFX;
    }

    public String getSubscriptionGroupInfoMessageKey() {
        return id + INFO_MESSAGE_KEY_SUFFIX;
    }

    public String getSubscriptionGroupFeatureMessageKey() {
        return id + FEATURES_MESSAGE_KEY_SUFFX;
    }

    public String getSubscriptionGroupPriceInfoMessageKey() {
        return id + PRICE_INFO_MESSAGE_KEY_SUFFIX;
    }
}
