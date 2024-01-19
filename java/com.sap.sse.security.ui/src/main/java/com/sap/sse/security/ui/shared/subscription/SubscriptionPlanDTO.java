package com.sap.sse.security.ui.shared.subscription;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanGroup;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;

/**
 * SubscriptionPlan data transfer object {@link SubscriptionService#getAllSubscriptionPlans()}
 */
public class SubscriptionPlanDTO implements HasSubscriptionMessageKeys, IsSerializable {
    private static final long serialVersionUID = -1990028347487353679L;
    private String id;
    private HashSet<SubscriptionPrice> prices;
    private String error;
    private boolean isUserSubscribedToPlan;
    private Set<PlanCategory> planCategory;
    private boolean userWasAlreadySubscribedToOneTimePlan;
    private boolean isUserSubscribedToAllPlanCategories;
    private boolean isOneOfTheUserSubscriptionsIsCoveredByPlan;
    private PlanGroup group;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, boolean isUserSubscribedToPlan, Set<SubscriptionPrice> prices,
            Set<PlanCategory> planCategory, boolean userWasAlreadySubscribedToOneTimePlan,
            boolean isUserSubscribedToAllPlanCategories, String error, PlanGroup group,
            boolean isOneOfTheUserSubscriptionsIsCoveredByPlan) {
        this.id = id;
        this.isUserSubscribedToPlan = isUserSubscribedToPlan;
        this.planCategory = planCategory;
        this.userWasAlreadySubscribedToOneTimePlan = userWasAlreadySubscribedToOneTimePlan;
        this.isUserSubscribedToAllPlanCategories = isUserSubscribedToAllPlanCategories;
        this.prices = new HashSet<SubscriptionPrice>(prices);
        this.error = error;
        this.group = group;
        this.isOneOfTheUserSubscriptionsIsCoveredByPlan = isOneOfTheUserSubscriptionsIsCoveredByPlan;
    }

    public PlanGroup getGroup() {
        return group;
    }

    public Set<PlanCategory> getPlanCategory() {
        return planCategory;
    }

    public boolean isUserWasAlreadySubscribedToOneTimePlan() {
        return userWasAlreadySubscribedToOneTimePlan;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String getSubscriptionPlanId() {
        return id;
    }

    public boolean isUserSubscribedToPlan() {
        return isUserSubscribedToPlan;
    }

    public String getError() {
        return error;
    }

    public boolean isOneOfTheUserSubscriptionsIsCoveredByPlan() {
        return isOneOfTheUserSubscriptionsIsCoveredByPlan;
    }

    public HashSet<SubscriptionPrice> getPrices() {
        return prices;
    }

    public boolean isUserSubscribedToAllPlanCategories() {
        return isUserSubscribedToAllPlanCategories;
    }

}
