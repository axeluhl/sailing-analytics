package com.sap.sse.security.shared;

import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.common.Util.Triple;

/**
 * For equality and hash code, the {@link RoleDefinition#getId() role definition ID}, the {@link Tenant#getId() tenant
 * ID} of a possible tenant qualifier, the {@link SecurityUser#getName() user name} as well as the
 * {@link AbstractRole#isTransitive() transitive flag} of a possible user qualifier are considered
 * <p>
 * 
 * The security system generally allows users to pass on permissions they have to other users. This permission to grant
 * permissions we call a "meta-permission" in this context. Permissions granted to a user through a role can be
 * restricted by {@link #isTransitive() setting their "transitive" flag} to {@code false}. In this case a user with such
 * a role cannot pass on the role's permissions unless the user has obtained the same permission through some other path
 * without a transitivity restriction. Non-transitive roles should, e.g., be used when the role has been obtained
 * through a payment / subscription system as passing on such permissions would undermine the whole paywall idea.
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class AbstractRole<RD extends RoleDefinition, G extends SecurityUserGroup<?>, U extends UserReference>
        implements Named {
    private static final long serialVersionUID = 1243342091492822614L;
    private static final String QUALIFIER_SEPARATOR = WildcardPermission.PART_DIVIDER_TOKEN;
    protected RD roleDefinition;
    protected G qualifiedForTenant;
    protected U qualifiedForUser;
    protected Boolean transitive;

    @Deprecated
    protected AbstractRole() {
    } // for GWT serialization only
    
    public AbstractRole(RD roleDefinition, Boolean transitive) {
        this(roleDefinition, /* tenant owner */ null, /* user owner */ null, transitive);
    }
    
    public AbstractRole(RD roleDefinition, G qualifiedForTenant, U qualifiedForUser, Boolean isTransitive) {
        if (roleDefinition == null) {
            throw new NullPointerException("A role's definition must not be null");
        }
        this.roleDefinition = roleDefinition;
        this.qualifiedForTenant = qualifiedForTenant;
        this.qualifiedForUser = qualifiedForUser;
        this.transitive = isTransitive;
    }
    
    @Override
    public String getName() {
        return roleDefinition.getName();
    }
    
    /**
     * Tells whether the permissions granted through this role shall be considered during a meta-permission check.
     * If {@code false} then the permissions granted through this role do not entitle the user with this role to
     * pass on this role's permissions. This doesn't exclude the possibility of the user having obtained one or more of the
     * same permissions through another path, such as explicit permission assignment or another role assignment with
     * {@link #isTransitive()} being {@code true} with that role implying one or more of the permissions this role
     * grants.
     */
    public Boolean isTransitive() {
        return this.transitive;
    }

    public RD getRoleDefinition() {
        return roleDefinition;
    }

    public Set<WildcardPermission> getPermissions() {
        return roleDefinition.getPermissions();
    }

    public G getQualifiedForTenant() {
        return qualifiedForTenant;
    }

    public U getQualifiedForUser() {
        return qualifiedForUser;
    }
    
    public Triple<String, String, String> getRoleDefinitionNameAndTenantQualifierNameAndUserQualifierName() {
        final String roleDefinitionName = roleDefinition == null ? null : roleDefinition.getName();
        final String tenantQualifierName = qualifiedForTenant == null ? null : qualifiedForTenant.getName();
        final String userQualifierName = qualifiedForUser == null ? null : qualifiedForUser.getName();
        return new Triple<>(roleDefinitionName, tenantQualifierName, userQualifierName);
    }

    @Override
    public String toString() {
        return getName()
                + ((getQualifiedForTenant() != null || getQualifiedForUser() != null) ? QUALIFIER_SEPARATOR : "")
                + (getQualifiedForTenant() != null ? getQualifiedForTenant().getName() : "")
                + (getQualifiedForUser() != null ? (QUALIFIER_SEPARATOR + getQualifiedForUser().getName()) : "");
    }

    /**
     * For hashing, the {@link RoleDefinition#getId() role definition ID}, the {@link Tenant#getId() tenant ID} of a
     * possible tenant qualifier, the {@link SecurityUser#getName() user name} of a possible user qualifier, as well as
     * the {@link AbstractRole#transitive transitive flag} are hashed.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiedForTenant == null) ? 0 : qualifiedForTenant.getId().hashCode());
        result = prime * result + ((qualifiedForUser == null) ? 0 : qualifiedForUser.getName().hashCode());
        result = prime * result + ((roleDefinition == null) ? 0 : roleDefinition.hashCode());
        result = prime * result + ((transitive == null) ? 0 : transitive.hashCode());
        return result;
    }

    /**
     * For equality, the {@link RoleDefinition role definition}, the {@link Tenant#getId() tenant ID} of a possible
     * tenant qualifier, the {@link SecurityUser#getName() user name} of a possible user qualifier, aswell as the
     * {@link AbstractRole#transitive transitive flag} are compared.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractRole other = (AbstractRole) obj;
        if (qualifiedForTenant == null) {
            if (other.qualifiedForTenant != null) {
                return false;
            }
        } else if (other.qualifiedForTenant == null) {
            return false;
        } else if (!qualifiedForTenant.getId().equals(other.qualifiedForTenant.getId()))
            return false;
        if (qualifiedForUser == null) {
            if (other.qualifiedForUser != null) {
                return false;
            }
        } else if (other.qualifiedForUser == null) {
            return false;
        } else if (!qualifiedForUser.getName().equals(other.qualifiedForUser.getName()))
            return false;
        if (roleDefinition == null) {
            if (other.roleDefinition != null)
                return false;
        } else if (!roleDefinition.equals(other.roleDefinition))
            return false;
        if (transitive == null) {
            if (other.transitive != null)
                return false;
        } else if (!transitive.equals(other.transitive))
            return false;
        return true;
    }
}
