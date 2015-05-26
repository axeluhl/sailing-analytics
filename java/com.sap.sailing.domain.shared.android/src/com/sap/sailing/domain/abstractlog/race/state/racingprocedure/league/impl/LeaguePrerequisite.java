package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.league.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.league.LeagueRacingProcedure;
import com.sap.sse.common.TimePoint;

/**
 * Easy access to the {@link LeagueRacingProcedure}.
 */
public abstract class LeaguePrerequisite extends BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    public LeaguePrerequisite(FulfillmentFunction function, LeagueRacingProcedure procedure, TimePoint originalNow,
            TimePoint originalStartTime) {
        super(function, procedure, originalNow, originalStartTime);
    }

    protected LeagueRacingProcedure getProcedure() {
        return (LeagueRacingProcedure) procedure;
    }

}
