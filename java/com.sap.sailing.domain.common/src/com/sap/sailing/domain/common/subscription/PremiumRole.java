package com.sap.sailing.domain.common.subscription;

import java.util.UUID;

import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

/**
 * Specifies a role that when associated to a user gives access to the streamlet Feature on the RaceMap
 */
public class PremiumRole extends RolePrototype {
    private static final UUID ROLE_ID = UUID.fromString("7021e7a2-569a-11ec-bf63-0242ac130002");
    private static final long serialVersionUID = 8032532973066767581L;
    private static final PremiumRole INSTANCE = new PremiumRole();
    private static final String MESSAGE_KEY = "premium_role";
    
    PremiumRole() {
        super("premium", ROLE_ID.toString(),
                WildcardPermission.builder().withTypes(SecuredSecurityTypes.USER)
                .withActions(SecuredSecurityTypes.UserActions.BE_PREMIUM).build());
    }

    public static PremiumRole getInstance() {
        return INSTANCE;
    }
    
    public static UUID getRoleId() {
        return ROLE_ID;
    }

    public static String getMessageKey() {
        return MESSAGE_KEY;
    }
}
