package com.sap.sailing.domain.racelog;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;

/**
 * This interfaces serves as a template for the actual RaceLogIdentifier that is compound as follows:
 * <p>
 * {LeaderboardName / RegattaName} + RaceColumnName + FleetName
 * <p>
 * As the components of the RaceLogIdentifier are not available on RaceColumn level, we have to propagate the
 * information in this template data object.
 */
public interface RaceLogIdentifierTemplate extends Serializable {
    
    /**
     * This method returns the name of parent object of the {@link RaceColumn}. This can be a
     * {@link FlexibleLeaderboard} or a {@link Regatta}.
     * 
     * @return the name of the parent object
     */
    String getParentObjectName();
    
    /**
     * Compiles the template with giving the last component, the {@link Fleet}, to a RaceLogIdentifier that is used to
     * retrieve the {@link RaceLogEvent}s from a RaceLogStore
     * 
     * @param fleet
     *            the fleet of the race
     * @return the RaceLogIdentifier
     */
    RaceLogIdentifier compileRaceLogIdentifier(Fleet fleet);
    
    /**
     * Resolves this RaceLogIdentifier template for replication with the help of the given resolver.
     * 
     * @param resolver
     *            the resolver that resolves this template, either for {@link Leaderboard} or for {@link Regatta}.
     */
    void resolve(RaceLogIdentifierTemplateResolver resolver);
    
    String getRaceColumnName();
}
