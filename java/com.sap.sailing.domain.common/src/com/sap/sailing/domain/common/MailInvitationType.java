package com.sap.sailing.domain.common;

public enum MailInvitationType {
    LEGACY,
    SailInsight1,
    SailInsight2,
    SailInsight3;

    public static final String SYSTEM_PROPERTY_NAME = "com.sap.sailing.domain.tracking.MailInvitationType";
}