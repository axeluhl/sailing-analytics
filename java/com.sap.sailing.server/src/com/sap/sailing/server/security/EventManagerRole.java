package com.sap.sailing.server.security;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.security.SecuredDomainType.EventActions;
import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;

/**
 * Specifies a role that when associated to a user gives read access to the sailing domain parts needed for the home
 * page and basic analytical frontends.
 */
public class EventManagerRole extends RolePrototype {
    private static final long serialVersionUID = 3291793984984443193L;

    private static final EventManagerRole INSTANCE = new EventManagerRole();

    EventManagerRole() {
        super("event_manager", "3d1fc58f-5afc-4823-b71d-049ab2e8a83d",
                WildcardPermission.builder().withTypes(SecuredSecurityTypes.SERVER).withActions(ServerActions.CREATE_OBJECT).build(),
                WildcardPermission.builder().withTypes(SecuredDomainType.EVENT).withActions(EventActions.UPLOAD_MEDIA).build());
    }

    public static EventManagerRole getInstance() {
        return INSTANCE;
    }
}
