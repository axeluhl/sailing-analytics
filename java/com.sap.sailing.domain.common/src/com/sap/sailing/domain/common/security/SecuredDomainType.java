package com.sap.sailing.domain.common.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.IdentifierStrategy;
import com.sap.sse.security.shared.impl.HasPermissionsImpl;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;

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
    
    /**
     * type-relative identifier is the event ID's string representation}
     */
    public static final HasPermissions EVENT = new SecuredDomainType("EVENT", IdentifierStrategy.ID);

    /**
     * type-relative identifier is the regatta name
     */
    public static final HasPermissions REGATTA = new SecuredDomainType("REGATTA", IdentifierStrategy.NAMED);

    /**
     * type-relative identifier is the leaderboard name
     */
    public static final HasPermissions LEADERBOARD = new SecuredDomainType("LEADERBOARD", IdentifierStrategy.NAMED);

    /**
     * type-relative identifier is the leaderboard group ID's string representation
     */
    public static final HasPermissions LEADERBOARD_GROUP = new SecuredDomainType("LEADERBOARD_GROUP", IdentifierStrategy.ID);

    /**
     * type-relative identifier is the regatta name and the race definition name, encoded using the
     * {@link WildcardPermissionEncoder#encodeStringList(String...)} method
     */
    //TODO: RaceDTO.getTypeRelativeIdentifierAsString
    public static final HasPermissions TRACKED_RACE = new SecuredDomainType("TRACKED_RACE", IdentifierStrategy.TO_SPECIFY);
    
    public static enum CompetitorAndBoatActions implements Action {
        READ_PUBLIC, READ_PRIVATE, LIST;
        
        private static final Action[] ALL_ACTIONS = new Action[] { READ_PUBLIC, READ_PRIVATE, LIST,
                DefaultActions.CREATE, DefaultActions.UPDATE, DefaultActions.CHANGE_OWNERSHIP,
                DefaultActions.CHANGE_ACL };
    };
    
    /**
     * type relative identifier is the competitor ID's string representation
     */
    public static final HasPermissions COMPETITOR = new SecuredDomainType("COMPETITOR", IdentifierStrategy.ID, CompetitorAndBoatActions.ALL_ACTIONS);
    
    /**
     * type relative identifier is the boat ID's string representation
     */
    public static final HasPermissions BOAT = new SecuredDomainType("BOAT", IdentifierStrategy.ID, CompetitorAndBoatActions.ALL_ACTIONS);
    
    /**
     * type-relative identifier is the media track's "DB ID"
     */
    public static final HasPermissions MEDIA_TRACK = new SecuredDomainType("MEDIA_TRACK", DomainIdentifierStrategy.MEDIA_TRACK);
    
    /**
     * the import URLs can be protected such that they take effect only for those users who can read them; type-relative
     * identifier is the {@link ScoreCorrectionProvider#getName() name of the score correction provider} and the URL,
     * encoded using the {@link WildcardPermissionEncoder#encodeStringList(String...)} method
     */
    public static final HasPermissions RESULT_IMPORT_URL = new SecuredDomainType("RESULT_IMPORT_URL", IdentifierStrategy.TO_SPECIFY);

    /**
     * Describes access permissions to {@code ExpeditionDeviceConfiguration} objects. Type-relative object identifier is
     * the WildcardPermissionEncoder.encode(getServerInfo().getServerName(), deviceConfiguration.getName());
     */
    public static final HasPermissions EXPEDITION_DEVICE_CONFIGURATION = new SecuredDomainType("EXPEDITION_DEVICE_CONFIGURATION", IdentifierStrategy.TO_SPECIFY);
    
    /**
     * Describes access permissions to Igtimi account objects. Type-relative
     * object identifier is the e-mail address string representing the account.
     */
    public static final HasPermissions IGTIMI_ACCOUNT = new SecuredDomainType("IGTIMI_ACCOUNT", IdentifierStrategy.TO_SPECIFY);
    
    /**
     * type-relative identifier is the jsonurl of the configuration: TracTracConfiguration::getJsonUrl
     */
    //TODO: TracTracConfiguration::getJSONURL
    public static final HasPermissions TRACTRAC_ACCOUNT = new SecuredDomainType("TRACTRAC_ACCOUNT", IdentifierStrategy.TO_SPECIFY);
    
    //TODO: SwissTimingConfiguration::getName
    public static final HasPermissions SWISS_TIMING_ACCOUNT = new SecuredDomainType("SWISS_TIMING_ACCOUNT", IdentifierStrategy.TO_SPECIFY);
    
    //TODO: SwissTimingArchiveConfiguration::getJsonUrl
    public static final HasPermissions SWISS_TIMING_ARCHIVE_ACCOUNT = new SecuredDomainType("SWISS_TIMING_ARCHIVE_ACCOUNT", IdentifierStrategy.TO_SPECIFY);
    
    public static enum ReplicatorActions implements Action { START, STOP, DROP_CONNECTION };
    /**
     * type-relative identifier is the server name
     */
    public static final HasPermissions REPLICATOR = new SecuredDomainType("REPLICATOR", IdentifierStrategy.TO_SPECIFY, ReplicatorActions.values());
    
    /**
     * Permission is checked against servername.
     */
    public static final HasPermissions DATA_MINING = new SecuredDomainType("DATA_MINING", IdentifierStrategy.STRING /* identifer is servername. */);

    /**
     * type-relative identifier is the device configuration name
     */
    public static final HasPermissions RACE_MANAGER_APP_DEVICE_CONFIGURATION = new SecuredDomainType("RACE_MANAGER_APP_DEVICE_CONFIGURATION", IdentifierStrategy.TO_SPECIFY);

    /**
     * Create required to do MasterDataImport, type-relative identifier is the servername
     */
    public static final HasPermissions CAN_IMPORT_MASTERDATA = new SecuredDomainType(
            "CAN_REPLAY_DURING_LIVE_RACES", IdentifierStrategy.TO_SPECIFY);
}
