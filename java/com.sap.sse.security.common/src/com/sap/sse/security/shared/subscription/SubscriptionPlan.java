package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 * @author Tu Tran
 */
public abstract class SubscriptionPlan implements Serializable {
    private static final long serialVersionUID = -555811806344107292L;
    private final String id;
    private final Set<SubscriptionPrice> prices;
    private final Set<PlanCategory> planCategory;
    private final Boolean isOneTimePlan;
    private final PlanGroup group;
    /**
     * Roles assigned for this plan, if user subscribe to the plan then the user will be assigned these roles
     */
    private final SubscriptionPlanRole[] roles;
    
    /*
     * Used to make Plans of the same category mutually exclusive.
     */
    public enum PlanCategory {
        PREMIUM("premium"), DATA_MINING_ARCHIVE("data_mining_archive"), DATA_MINING_ALL("data_mining_all");
        final String id;
        PlanCategory(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }
    }
    
    /**
     * Used to organize plans in groups to show on one subscription card. 
     * The order of this enumeration is usually also the main order on the UI.
     */
    public enum PlanGroup {
        PREMIUM("premium"), DATA_MINING_ARCHIVE("data_mining_archive"), DATA_MINING_ALL("data_mining_all"), TRIAL("trial");
        final String id;
        PlanGroup(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }
    }
    
    protected SubscriptionPlan(String id, Set<SubscriptionPrice> prices, Set<PlanCategory> planCategory,
            Boolean isOneTimePlan, PlanGroup group, SubscriptionPlanRole[] roles) {
        this.id = id;
        this.roles = roles;
        this.prices = prices;
        this.planCategory = planCategory;
        this.isOneTimePlan = isOneTimePlan;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public Set<SubscriptionPrice> getPrices() {
        return prices;
    }

    public SubscriptionPlanRole[] getRoles() {
        return roles;
    }
    
    public Set<PlanCategory> getPlanCategories() {
        return planCategory;
    }
    
    public Boolean getIsOneTimePlan() {
        return isOneTimePlan;
    }
    
    public PlanGroup getGroup() {
        return group;
    }

    public boolean isUserInPossessionOfRoles(User user) {
        boolean foundAll = true;
        for (SubscriptionPlanRole planRole : getRoles()) {
            boolean found = false;
            for (Role userRole : user.getRoles()) {
                if (userRole.getRoleDefinition().getId().equals(planRole.getRoleId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                foundAll = false;
                break;
            }
        }
        return foundAll;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + Arrays.hashCode(roles);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionPlan other = (SubscriptionPlan) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (!Arrays.equals(roles, other.roles))
            return false;
        return true;
    }
}
