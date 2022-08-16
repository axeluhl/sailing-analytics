package com.sap.sse.security.shared.subscription;

import java.io.Serializable;
import java.util.UUID;

/**
 * Specify role UUID associated with a plan with qualification. Role qualification (by group/user ownership) can be
 * specified by rules, such as qualifying for objects owned by the subscribing user, qualified by the subscribing user's
 * own default group, etc. or by specific user name, and/or user group name. Choose {@code NONE} or {@code null} for the
 * user / group qualification mode if you want to provide an explicit user / group for qualification, respectively.
 * 
 * @author Tu Tran
 */
public class SubscriptionPlanRole implements Serializable{
    private static final long serialVersionUID = -4052966548617597414L;

    /**
     * Specify how role is qualified by user: none(unqualified) or by subscription user
     */
    public static enum UserQualificationMode {
        /**
         * Unqualified
         */
        NONE,
        /**
         * Qualified by subscribing user
         */
        SUBSCRIBING_USER
    }

    /**
     * Specify how role is qualified by user group: {@link #NONE NONE (unqualified)} allows for an explicit group to be
     * specified optionally for a user qualification, by qualified user default group (<tt>{username}-tenant</tt>), or
     * by subscribed user default tenant
     */
    public static enum GroupQualificationMode {
        /**
         * Unqualified; an explicit group qualification may be specified instead 
         */
        NONE,
        /**
         * Default tenant of qualified user; if the user qualification is chosen to be the
         * {@link UserQualificationMode#SUBSCRIBING_USER subscribing user}, then the subscribing user's default
         * group/tenant is used. If an explicit user qualification for a user other than the subscribing user has been
         * selected, then that user's default group/tenant will be used.
         */
        DEFAULT_QUALIFIED_USER_TENANT,
        /**
         * Default tenant of subscribing user; even if an explicit user other than the subscribing user is selected for
         * the user qualification of the role, still the subscribing user's default group/tenant will be used as group
         * qualification for the role assigned.
         */
        SUBSCRIBING_USER_DEFAULT_TENANT
    }

    private final UUID roleId;
    private final UserQualificationMode userQualificationMode;
    private final GroupQualificationMode groupQualificationMode;

    /**
     * A specific qualified tenant name
     */
    private final UUID idOfExplicitGroupQualification;

    /**
     * A specific qualified user name
     */
    private final String explicitUserQualification;

    public SubscriptionPlanRole(UUID roleId, GroupQualificationMode groupQualificationMode,
            UserQualificationMode userQualificationMode) {
        this(roleId, groupQualificationMode, userQualificationMode, /* explicitUserQualification */ null, /* explicitGroupQualification */ null);
    }

    /**
     * @param groupQualificationMode
     *            must be {@code null} or {@link UserQualificationMode#NONE} in case a non-{@code null}
     *            {@code idOfExplicitGroupQualification} is specified
     * @param userQualificationMode
     *            must be {@code null} or {@link GroupQualificationMode#NONE} in case a non-{@code null}
     *            {@code explicitUserQualification} is specified
     */
    public SubscriptionPlanRole(UUID roleId,
            GroupQualificationMode groupQualificationMode, UserQualificationMode userQualificationMode,
            String explicitUserQualification, UUID idOfExplicitGroupQualification) {
        if (explicitUserQualification != null
                && (userQualificationMode != null && userQualificationMode != UserQualificationMode.NONE)) {
            throw new IllegalArgumentException("Explicit user qualification provided, but user qualification mode "
                    + userQualificationMode + " hides it.");
        }
        if (idOfExplicitGroupQualification != null
                && (groupQualificationMode != null && groupQualificationMode != GroupQualificationMode.NONE)) {
            throw new IllegalArgumentException("Explicit groupo qualification provided, but group qualification mode "
                    + groupQualificationMode + " hides it.");
        }
        this.roleId = roleId;
        this.userQualificationMode = userQualificationMode;
        this.groupQualificationMode = groupQualificationMode;
        this.explicitUserQualification = explicitUserQualification;
        this.idOfExplicitGroupQualification = idOfExplicitGroupQualification;
    }

    public SubscriptionPlanRole(UUID roleId) {
        this(roleId, /* groupQualificationMode */ null, /* userQualificationMode */ null,
                /* explicitUserQualfication */ null, /* explicitGroupQualification */ null);
    }

    public UUID getRoleId() {
        return roleId;
    }

    public UserQualificationMode getUserQualificationMode() {
        return userQualificationMode;
    }

    public GroupQualificationMode getGroupQualificationMode() {
        return groupQualificationMode;
    }

    public UUID getIdOfExplicitGroupQualification() {
        return idOfExplicitGroupQualification;
    }

    public String getExplicitUserQualification() {
        return explicitUserQualification;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((explicitUserQualification == null) ? 0 : explicitUserQualification.hashCode());
        result = prime * result + ((groupQualificationMode == null) ? 0 : groupQualificationMode.hashCode());
        result = prime * result
                + ((idOfExplicitGroupQualification == null) ? 0 : idOfExplicitGroupQualification.hashCode());
        result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
        result = prime * result + ((userQualificationMode == null) ? 0 : userQualificationMode.hashCode());
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
        SubscriptionPlanRole other = (SubscriptionPlanRole) obj;
        if (explicitUserQualification == null) {
            if (other.explicitUserQualification != null)
                return false;
        } else if (!explicitUserQualification.equals(other.explicitUserQualification))
            return false;
        if (groupQualificationMode != other.groupQualificationMode)
            return false;
        if (idOfExplicitGroupQualification == null) {
            if (other.idOfExplicitGroupQualification != null)
                return false;
        } else if (!idOfExplicitGroupQualification.equals(other.idOfExplicitGroupQualification))
            return false;
        if (roleId == null) {
            if (other.roleId != null)
                return false;
        } else if (!roleId.equals(other.roleId))
            return false;
        if (userQualificationMode != other.userQualificationMode)
            return false;
        return true;
    }

}
