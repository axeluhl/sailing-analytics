package com.sap.sse.security.shared.subscription;

import java.util.Arrays;

import com.sap.sse.common.NamedWithID;

/**
 * Payment subscription plans. A subscription plan has a name, a {@link String}-based ID, and a set of
 * {@link SubscriptionPlanRole roles} it grants to a subscribing user. These roles can specify how they are to be
 * qualified when assigned, regarding user and group qualifications. See
 * {@link SubscriptionPlanRole.GroupQualificationMode} and {@link SubscriptionPlanRole.UserQualificationMode} for
 * more details.
 * 
 * @author Tu Tran
 */
public abstract class SubscriptionPlan implements NamedWithID{
    private static final long serialVersionUID = -555811806344107292L;
    private final String name;
    private final String id;
    /**
     * Roles assigned for this plan, if user subscribe to the plan then the user will be assigned these roles
     */
    private final SubscriptionPlanRole[] roles;
    
    protected SubscriptionPlan(String id, String name, SubscriptionPlanRole[] roles) {
        this.name = name;
        this.id = id;
        this.roles = roles;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getId() {
        return id;
    }

    public SubscriptionPlanRole[] getRoles() {
        return this.roles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (!Arrays.equals(roles, other.roles))
            return false;
        return true;
    }
}
