package com.sap.sse.security.shared;

import java.util.UUID;

/**
 * Specify role UUID associated with a plan with qualification. Role qualification (by group/user ownership) can be
 * specified by rules, such as qualifying for objetcs owned by the subscribing user, qualified by the subscribing user's
 * own default group, etc. or by specific user name, and/or user group name.
 * 
 * @author Tu Tran
 */
public class SubscriptionPlanRole {
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
     * Specify how role is qualified by user group: {@link #NONE NONE (unqualified)}, by qualified user default group
     * (<tt>{username}-tenant</tt>), or by subscribed user default tenant
     */
    public static enum GroupQualificationMode {
        /**
         * Unqualified
         */
        NONE,
        /**
         * Default tenant of qualified user
         */
        DEFAULT_QUALIFIED_USER_TENANT,
        /**
         * Default tenant of subscribing user
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

    public SubscriptionPlanRole(UUID roleId, GroupQualificationMode groupQualificationMode,
            UserQualificationMode userQualificationMode, String explicitUserQualfication, UUID idOfExplicitGroupQualification) {
        this.roleId = roleId;
        this.userQualificationMode = userQualificationMode;
        this.groupQualificationMode = groupQualificationMode;
        this.explicitUserQualification = explicitUserQualfication;
        this.idOfExplicitGroupQualification = idOfExplicitGroupQualification;
    }

    public SubscriptionPlanRole(UUID roleId) {
        this(roleId, /* groupQualificationMode */ null, /* userQualificationMode */ null, /* explicitUserQualfication */ null, /* explicitGroupQualification */ null);
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

}
