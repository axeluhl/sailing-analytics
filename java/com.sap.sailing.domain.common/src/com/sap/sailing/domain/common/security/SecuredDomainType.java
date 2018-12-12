package com.sap.sailing.domain.common.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.HasPermissionsImpl;

/**
 * Logical domain types in the "sailing" domain that require the user to have certain permissions
 * in order to use their actions. These types are defined here in the "common" bundle so that
 * the server as well as the client can check them.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SecuredDomainType extends HasPermissionsImpl {
    private static final long serialVersionUID = -7072719056136061490L;
    private static final Set<HasPermissions> allInstances = new HashSet<>();
    
    public SecuredDomainType(String logicalTypeName, IdentifierStrategy identiferStrategy, Action... availableActions) {
        super(logicalTypeName, identiferStrategy, availableActions);
        allInstances.add(this);
    }
    
    public SecuredDomainType(String logicalTypeName, IdentifierStrategy identiferStrategy) {
        super(logicalTypeName, identiferStrategy);
        allInstances.add(this);
    }
    
    public static Iterable<HasPermissions> getAllInstances() {
        return Collections.unmodifiableSet(allInstances);
    }
    
    // AdminConsole permissions
    public static final HasPermissions MANAGE_MARK_PASSINGS = new SecuredDomainType("MANAGE_MARK_PASSINGS", IdentifierStrategy.NO_OP);
    public static final HasPermissions MANAGE_MARK_POSITIONS = new SecuredDomainType("MANAGE_MARK_POSITIONS", IdentifierStrategy.NO_OP);
    public static final HasPermissions CAN_REPLAY_DURING_LIVE_RACES = new SecuredDomainType("CAN_REPLAY_DURING_LIVE_RACES", IdentifierStrategy.NO_OP);
    public static final HasPermissions DETAIL_TIMER = new SecuredDomainType("DETAIL_TIMER", IdentifierStrategy.NO_OP); // TODO this is not a valid "HasPermission" instance; it's more an operation the user may be granted on objects of the TimePanel type
    
    public static final HasPermissions EVENT = new SecuredDomainType("EVENT", IdentifierStrategy.ID);

    public static final HasPermissions REGATTA = new SecuredDomainType("REGATTA", IdentifierStrategy.NAMED);

    public static final HasPermissions LEADERBOARD = new SecuredDomainType("LEADERBOARD", IdentifierStrategy.NAMED);

    public static final HasPermissions LEADERBOARD_GROUP = new SecuredDomainType("LEADERBOARD_GROUP", IdentifierStrategy.ID);

    public static final HasPermissions TRACKED_RACE = new SecuredDomainType("TRACKED_RACE", DomainIdentifierStrategy.TRACKED_RACE);
    
    public static enum CompetitorAndBoatActions implements Action {
        READ_PUBLIC;
        
        private static final Action[] ALL_ACTIONS = new Action[] { READ_PUBLIC, DefaultActions.READ,
                DefaultActions.CREATE, DefaultActions.UPDATE, DefaultActions.CHANGE_OWNERSHIP,
                DefaultActions.CHANGE_ACL };
    };

    public static final HasPermissions COMPETITOR = new SecuredDomainType("COMPETITOR", IdentifierStrategy.ID, CompetitorAndBoatActions.ALL_ACTIONS);

    public static final HasPermissions BOAT = new SecuredDomainType("BOAT", IdentifierStrategy.ID, CompetitorAndBoatActions.ALL_ACTIONS);

    public static final HasPermissions MEDIA_TRACK = new SecuredDomainType("MEDIA_TRACK", DomainIdentifierStrategy.MEDIA_TRACK);

    public static final HasPermissions RESULT_IMPORT_URL = new SecuredDomainType("RESULT_IMPORT_URL", DomainIdentifierStrategy.RESULT_IMPORT_URL);

    public static enum ReplicatorActions implements Action {
        START, STOP, DROP_CONNECTION
    };

    public static final HasPermissions REPLICATOR = new SecuredDomainType("REPLICATOR", IdentifierStrategy.SERVERNAME,
            ReplicatorActions.values());

    /**
     * This permission is used to check READ-permission on different things. For that the object type to determine the
     * permission strings is String (e.g. servername, DataRetrieverChainDefinitionDTO.name, RetrieverChainDefinition.
     * name, QueryIdentifier, ...)
     */
    public static final HasPermissions DATA_MINING = new SecuredDomainType("DATA_MINING",
            IdentifierStrategy.STRING);

    public static final HasPermissions RACE_MANAGER_APP_DEVICE_CONFIGURATION = new SecuredDomainType(
            "RACE_MANAGER_APP_DEVICE_CONFIGURATION", IdentifierStrategy.NAMED);

}
