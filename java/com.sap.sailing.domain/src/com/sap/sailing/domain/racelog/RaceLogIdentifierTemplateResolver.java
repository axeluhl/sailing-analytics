package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.racelog.impl.RaceLogOnLeaderboardIdentifier;
import com.sap.sailing.domain.racelog.impl.RaceLogOnRegattaIdentifier;

/**
 * This interface is used for replication purposes. It resolves the {@link RaceLogIdentiferTemplate} to its components 
 * ({@link FlexibleLeaderboard}/{@link Regatta} Name, RaceColumn Name and Fleet Name to create a replication operation,
 * depending on the type of the parentObject in the template.
 */
public interface RaceLogIdentifierTemplateResolver {
    
    /**
     * Resolves a identifier template for regattas and replicates a RaceLogEvent
     * @param identifierTemplate the Regatta identifier template
     */
    void resolveOnRegattaIdentifierAndReplicate(RaceLogOnRegattaIdentifier identifierTemplate);
    
    /**
     * Resolves a identifier template for flexible leaderboards and replicates a RaceLogEvent
     * @param identifierTemplate the FlexibleLeaderboard identifier template
     */
    void resolveOnLeaderboardIdentifierAndReplicate(RaceLogOnLeaderboardIdentifier identifierTemplate);
}
