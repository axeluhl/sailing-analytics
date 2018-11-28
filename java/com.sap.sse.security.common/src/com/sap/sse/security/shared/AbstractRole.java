package com.sap.sse.security.shared;

import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.common.Util.Triple;

/**
 * For equality and hash code, the {@link RoleDefinition#getId() role definition ID}, the {@link Tenant#getId() tenant ID} of a
 * possible tenant qualifier as well as the {@link SecurityUser#getName() user name} of a possible user qualifier
 * are considered
 * 
 * @author Axel Uhl (D043530)
 */
public abstract class AbstractRole<RD extends RoleDefinition, G extends AbstractUserGroup<?>, U extends SecurityUser<RD, ?, G>>
        implements Named {
    private static final String QUALIFIER_SEPARATOR = WildcardPermission.PART_DIVIDER_TOKEN;
    private static final long serialVersionUID = 1243342091492822614L;
    private RD roleDefinition;
    protected G qualifiedForTenant;
    protected U qualifiedForUser;
    
    public static Triple<String, String, String> getRoleDefinitionNameAndTenantQualifierNameAndUserQualifierName(String roleAsString) {
        final String[] split = roleAsString.split(QUALIFIER_SEPARATOR);
        return new Triple<>(split[0], split.length<2?null:split[1], split.length<3?null:split[2]);
    }
    
    @Deprecated
    protected AbstractRole() {
    } // for GWT serialization only
    
    public AbstractRole(RD roleDefinition) {
        this(roleDefinition, /* tenant owner */ null, /* user owner */ null);
    }
    
    public AbstractRole(RD roleDefinition, G qualifiedForTenant, U qualifiedForUser) {
        if (roleDefinition == null) {
            throw new NullPointerException("A role's definition must not be null");
        }
        this.roleDefinition = roleDefinition;
        this.qualifiedForTenant = qualifiedForTenant;
        this.qualifiedForUser = qualifiedForUser;
    }

    @Override
    public String getName() {
        return roleDefinition.getName();
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
    
    @Override
    public String toString() {
        return getName()+((getQualifiedForTenant()!=null || getQualifiedForUser()!= null)?QUALIFIER_SEPARATOR:"")+
                (getQualifiedForTenant()!=null?getQualifiedForTenant().getName():"")+
                        (getQualifiedForUser()!=null?(QUALIFIER_SEPARATOR+getQualifiedForUser().getName()):"");
    }

    /**
     * For hashing, the {@link RoleDefinition#getId() role definition ID}, the {@link Tenant#getId() tenant ID} of a
     * possible tenant qualifier as well as the {@link SecurityUser#getName() user name} of a possible user qualifier
     * are hashed.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiedForTenant == null) ? 0 : qualifiedForTenant.hashCode());
        result = prime * result + ((qualifiedForUser == null) ? 0 : qualifiedForUser.getName().hashCode());
        result = prime * result + ((roleDefinition == null) ? 0 : roleDefinition.hashCode());
        return result;
    }

    /**
     * For equality, the {@link RoleDefinition#getId() role definition ID}, the {@link Tenant#getId() tenant ID} of a
     * possible tenant qualifier as well as the {@link SecurityUser#getName() user name} of a possible user qualifier
     * are compared.
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
            if (other.qualifiedForTenant != null)
                return false;
        } else if (!qualifiedForTenant.equals(other.qualifiedForTenant))
            return false;
        if (qualifiedForUser == null) {
            if (other.qualifiedForUser != null)
                return false;
        } else if (!qualifiedForUser.getName().equals(other.qualifiedForUser.getName()))
            return false;
        if (roleDefinition == null) {
            if (other.roleDefinition != null)
                return false;
        } else if (!roleDefinition.equals(other.roleDefinition))
            return false;
        return true;
    }
}
