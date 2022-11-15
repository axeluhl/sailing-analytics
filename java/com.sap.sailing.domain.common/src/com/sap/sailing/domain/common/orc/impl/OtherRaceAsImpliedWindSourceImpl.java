package com.sap.sailing.domain.common.orc.impl;

import com.sap.sailing.domain.common.orc.ImpliedWind;
import com.sap.sailing.domain.common.orc.OtherRaceAsImpliedWindSource;
import com.sap.sse.common.Util.Triple;

/**
 * A simple implementation of the {@link ImpliedWind} interface that carries all values for all
 * get methods as immutable fields. This lends itself well for serialization purposes.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class OtherRaceAsImpliedWindSourceImpl implements OtherRaceAsImpliedWindSource {
    private static final long serialVersionUID = 1353122921161718934L;
    private final Triple<String, String, String> leaderboardAndRaceColumnAndFleetOfDefiningRace;
    
    public OtherRaceAsImpliedWindSourceImpl(Triple<String, String, String> leaderboardAndRaceColumnAndFleetOfDefiningRace) {
        super();
        this.leaderboardAndRaceColumnAndFleetOfDefiningRace = leaderboardAndRaceColumnAndFleetOfDefiningRace;
    }

    @Override
    public Triple<String, String, String> getLeaderboardAndRaceColumnAndFleetOfDefiningRace() {
        return leaderboardAndRaceColumnAndFleetOfDefiningRace;
    }

    @Override
    public String toString() {
        return "from race " + leaderboardAndRaceColumnAndFleetOfDefiningRace.getB() + " of fleet "
                + leaderboardAndRaceColumnAndFleetOfDefiningRace.getC() + " in leaderboard "
                + leaderboardAndRaceColumnAndFleetOfDefiningRace.getA();
    }
}