package com.sap.sailing.domain.common.subscription;

import java.util.UUID;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.RolePrototype;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * Specifies a role that when associated to a user gives access to the streamlet Feature on the RaceMap
 */
public class StreamletViewerRole extends RolePrototype {
    private static final UUID ROLE_ID = UUID.fromString("f7912343-2091-4925-b815-8f68a4414eff");
    private static final long serialVersionUID = 3291793984984443193L;
    private static final StreamletViewerRole INSTANCE = new StreamletViewerRole();
    private static final String MESSAGE_KEY = "streamlet_viewer_role";
    
    StreamletViewerRole() {
        super("streamletViewer", ROLE_ID.toString(),
                WildcardPermission.builder().withTypes(SecuredDomainType.TRACKED_RACE)
                        .withActions(SecuredDomainType.TrackedRaceActions.VIEWSTREAMLETS).build(),
                WildcardPermission.builder().withTypes(SecuredDomainType.SIMULATOR)
                        .withActions(DefaultActions.READ).build(),
                WildcardPermission.builder().withTypes(SecuredDomainType.TRACKED_RACE)
                        .withActions(SecuredDomainType.TrackedRaceActions.SIMULATOR).build());
    }

    public static StreamletViewerRole getInstance() {
        return INSTANCE;
    }
    
    public static UUID getRoleId() {
        return ROLE_ID;
    }

    public static String getMessageKey() {
        return MESSAGE_KEY;
    }
}
