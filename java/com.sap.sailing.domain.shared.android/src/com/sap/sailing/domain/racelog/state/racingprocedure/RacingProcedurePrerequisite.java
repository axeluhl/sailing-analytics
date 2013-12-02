package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl.StartmodePrerequisite;

public interface RacingProcedurePrerequisite {

    public interface Resolver {
        void fulfill(PathfinderPrerequisite prerequisite);

        void fulfill(GateLaunchTimePrerequisite prerequisite);

        void fulfill(StartmodePrerequisite prerequisite);
    }

    public interface FulfillmentFunction {
        void execute();
    }

    /**
     * Resolve this chain of {@link RacingProcedurePrerequisite} with the help of a passed {@link Resolver}.
     */
    void resolve(Resolver resolver);
    
    void fulfillWithDefault();
}
