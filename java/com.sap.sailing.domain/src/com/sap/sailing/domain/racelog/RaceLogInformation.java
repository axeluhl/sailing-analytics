package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;

/**
 * This interface holds the information for a {@link RaceColumn} needed to have access to its {@link RaceLog}s. 
 * The needed information are the {@link RaceLogStore} to retrieve the {@link RaceLog}s, the {@link RaceLogIdentifierTemplate} to 
 * generate the
 * appropriate {@link RaceLogIdentifier} for persistence and the {@link RaceLog} for a given {@link RaceColumn} and {@link Fleet}
 * (parentObject is hold in the identifier template).
 */
public interface RaceLogInformation {
    /**
     * Gives the {@link RaceLogStore} for a {@link RaceColumn}
     * @return a {@link RaceLogStore}
     */
    RaceLogStore getStore();
    
    /**
     * Gives the {@link RaceLogIdentifierTemplate} containing the Parent Object of the {@link RaceColumn}
     * ({@link FlexibleLeaderboard} or {@link Regatta})
     * @return a {@link RaceLogIdentifierTemplate} 
     */
    RaceLogIdentifierTemplate getIdentifierTemplate();
    
    
    /**
     * Retrieves a {@link RaceLog} for the given race (combination of {@link RaceColumn} and {@link Fleet})
     * by compiling the actual {@link RaceLogIdentifier} from the identifier template
     * @param raceColumn the {@link RaceColumn} for which the {@link RaceLog} shall be retrieved
     * @param fleet the {@link Fleet} for which the {@link RaceLog} shall be retrieved
     * @return the {@link RaceLog} for a combination of {@link FlexibleLeaderboard}/{@link Regatta}, {@link RaceColumn} 
     * and {@link Fleet}
     */
    RaceLog getRaceLog(RaceColumn raceColumn, Fleet fleet);
    
}
