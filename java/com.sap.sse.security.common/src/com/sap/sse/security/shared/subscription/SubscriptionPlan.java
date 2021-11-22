package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 * @author Tu Tran
 */
public abstract class SubscriptionPlan implements Serializable{
    private static final long serialVersionUID = -555811806344107292L;
    private final String id;
    private final Set<SubscriptionPrice> prices;
    /**
     * Roles assigned for this plan, if user subscribe to the plan then the user will be assigned these roles
     */
    private final SubscriptionPlanRole[] roles;
    
    protected SubscriptionPlan(String id, Set<SubscriptionPrice> prices, SubscriptionPlanRole[] roles) {
        this.id = id;
        this.roles = roles;
        this.prices = prices;
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
