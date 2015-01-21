package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.Resolver;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.impl.StartmodePrerequisite;

/**
 * Always calls {@link RacingProcedurePrerequisite#fulfillWithDefault()}.
 */
public class RacingProcedurePrerequisiteAutoResolver implements Resolver {

    @Override
    public void fulfill(PathfinderPrerequisite prerequisite) {
        prerequisite.fulfillWithDefault();
    }

    @Override
    public void fulfill(GateLaunchTimePrerequisite prerequisite) {
        prerequisite.fulfillWithDefault();
    }

    @Override
    public void fulfill(StartmodePrerequisite prerequisite) {
        prerequisite.fulfillWithDefault();
    }

    @Override
    public void onFulfilled() {
        
    }

}
