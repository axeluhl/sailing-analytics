package com.sap.sse.security.ui.shared.subscription;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanCategory;
import com.sap.sse.security.shared.subscription.SubscriptionPlan.PlanGroup;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;

/**
 * SubscriptionPlan data transfer object {@link SubscriptionService#getAllSubscriptionPlans()}
 */
public class SubscriptionPlanDTO implements HasSubscriptionMessageKeys, IsSerializable {
    private static final long serialVersionUID = -1990028347487353679L;
    private String id;
    private HashSet<SubscriptionPrice> prices = new HashSet<SubscriptionPrice>();
    private String error;
    private Boolean isUserSubscribedToPlan;
    private Set<PlanCategory> planCategory;
    private Boolean userWasAlreadySubscribedToOneTimePlan;
    private Boolean isUserSubscribedToAllPlanCategories;
    private PlanGroup group;

    /**
     * For GWT Serialization only
     */
    @Deprecated
    public SubscriptionPlanDTO() {
    }

    public SubscriptionPlanDTO(String id, boolean isUserSubscribedToPlan, Set<SubscriptionPrice> prices,
            Set<PlanCategory> planCategory, boolean userWasAlreadySubscribedToOneTimePlan,
            boolean isUserSubscribedToAllPlanCategories, String error, PlanGroup group) {
        this.id = id;
        this.isUserSubscribedToPlan = isUserSubscribedToPlan;
        this.planCategory = planCategory;
        this.userWasAlreadySubscribedToOneTimePlan = userWasAlreadySubscribedToOneTimePlan;
        this.isUserSubscribedToAllPlanCategories = isUserSubscribedToAllPlanCategories;
        this.prices = new HashSet<SubscriptionPrice>(prices);
        this.error = error;
        this.group = group;
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
    
    // TODO: Implement action information
    public boolean isCurrentUserSubscribed() {
        return false;
    }

    public HashSet<SubscriptionPrice> getPrices() {
        return prices;
    }

    public boolean isUserSubscribedToAllPlanCategories() {
        return isUserSubscribedToAllPlanCategories;
    }

    public void setError(String error) {
        this.error = error;
    }

}
