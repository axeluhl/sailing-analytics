package com.sap.sse.security.shared;

import java.util.Set;

public class RoleImpl implements Role {
    private static final long serialVersionUID = 1243342091492822614L;
    private RoleDefinition roleDefinition;
    private Tenant qualifiedForTenant;
    private SecurityUser qualifiedForUser;
    
    @Deprecated
    RoleImpl() {} // for GWT serialization only
    
    public RoleImpl(RoleDefinition roleDefinition) {
        this(roleDefinition, /* tenant owner */ null, /* user owner */ null);
    }
    
    public RoleImpl(RoleDefinition roleDefinition, Tenant qualifiedForTenant, SecurityUser qualifiedForUser) {
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

    @Override
    public RoleDefinition getRoleDefinition() {
        return roleDefinition;
    }

    @Override
    public Set<WildcardPermission> getPermissions() {
        return roleDefinition.getPermissions();
    }

    @Override
    public Tenant getQualifiedForTenant() {
        return qualifiedForTenant;
    }

    @Override
    public SecurityUser getQualifiedForUser() {
        return qualifiedForUser;
    }
    
    @Override
    public String toString() {
        return getName()+((getQualifiedForTenant()!=null || getQualifiedForUser()!= null)?":":"")+
                (getQualifiedForTenant()!=null?getQualifiedForTenant().getName():"")+
                        (getQualifiedForUser()!=null?(":"+getQualifiedForUser().getName()):"");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiedForTenant == null) ? 0 : qualifiedForTenant.hashCode());
        result = prime * result + ((qualifiedForUser == null) ? 0 : qualifiedForUser.hashCode());
        result = prime * result + ((roleDefinition == null) ? 0 : roleDefinition.hashCode());
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
        RoleImpl other = (RoleImpl) obj;
        if (qualifiedForTenant == null) {
            if (other.qualifiedForTenant != null)
                return false;
        } else if (!qualifiedForTenant.equals(other.qualifiedForTenant))
            return false;
        if (qualifiedForUser == null) {
            if (other.qualifiedForUser != null)
                return false;
        } else if (!qualifiedForUser.equals(other.qualifiedForUser))
            return false;
        if (roleDefinition == null) {
            if (other.roleDefinition != null)
                return false;
        } else if (!roleDefinition.equals(other.roleDefinition))
            return false;
        return true;
    }
}
