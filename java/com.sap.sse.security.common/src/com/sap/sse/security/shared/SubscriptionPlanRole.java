package com.sap.sse.security.shared;

import java.util.UUID;

/**
 * Specify role UUID associated with a plan with qualification. Qualification can be specified by rules, such qualified
 * by subscription user, qualified by default user tenant..., or by specific user name, and/or tenant name
 * 
 * @author Tu Tran
 */
public class SubscriptionPlanRole {
    /**
     * Specify how role is qualified by user: none(unqualified) or by subscription user
     */
    public static enum UserQualification {
        /**
         * Unqualified
         */
        NONE,
        /**
         * Qualified by subscription user
         */
        USER
    }

    /**
     * Specify how role is qualified by tenant: none(unqualified), by qualified user default tenant, or by subscribed
     * user default tenant
     */
    public static enum TenantQualification {
        /**
         * Unqualified
         */
        NONE,
        /**
         * Default tenant of qualified user
         */
        DEFAULT_QUALIFIED_USER_TENANT,
        /**
         * Default tenant of subscription user
         */
        DEFAULT_SUBSCRIBED_USER_TENANT
    }

    private UUID roleId;
    private UserQualification userQualification;
    private TenantQualification tenantQualification;

    /**
     * A specific qualified tenant name
     */
    private String tenantName;

    /**
     * A specific qualified user name
     */
    private String userName;

    public SubscriptionPlanRole(UUID roleId, TenantQualification tenantQualification,
            UserQualification userQualification) {
        this.roleId = roleId;
        this.userQualification = userQualification;
        this.tenantQualification = tenantQualification;
    }

    public SubscriptionPlanRole(UUID roleId, String tenantName, UserQualification userQualification) {
        this.roleId = roleId;
        this.userQualification = userQualification;
        this.tenantName = tenantName;
    }

    public SubscriptionPlanRole(UUID roleId, TenantQualification tenantQualification, String userName) {
        this.roleId = roleId;
        this.tenantQualification = tenantQualification;
        this.userName = userName;
    }

    public SubscriptionPlanRole(UUID roleId, String tenantName, String userName) {
        this.roleId = roleId;
        this.tenantName = tenantName;
        this.userName = userName;
    }

    public SubscriptionPlanRole(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public UserQualification getUserQualification() {
        return userQualification;
    }

    public TenantQualification getTenantQualification() {
        return tenantQualification;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getUserName() {
        return userName;
    }

}
