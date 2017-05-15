package com.sap.sailing.domain.abstractlog.race.state.racingprocedure;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl.StartModePrerequisite;

/**
 * Something that has to be fulfilled before the race can start (e.g. decide which start mode flag is shown).
 */
public interface RacingProcedurePrerequisite {

    /**
     * In charge of fulfilling all {@link RacingProcedurePrerequisite}. Implementations must call the
     * {@link RacingProcedurePrerequisite#resolve(Resolver)} or {@link RacingProcedurePrerequisite#fulfillWithDefault()}
     * methods to let the framework decide whether there are more prerequisites or the race can start.
     */
    public interface Resolver {
        /**
         * Called when all {@link RacingProcedurePrerequisite} are fulfilled.
         */
        void onFulfilled();

        void fulfill(PathfinderPrerequisite prerequisite);

        void fulfill(GateLaunchTimePrerequisite prerequisite);

        void fulfill(StartModePrerequisite prerequisite);
    }

    /**
     * Interface used by the framework for executing the setting of a new start time after all prerequisites are
     * fulfilled.
     */
    public interface FulfillmentFunction {
        void execute();
    }

    /**
     * Resolve this {@link RacingProcedurePrerequisite} with the help of a passed {@link Resolver}.
     */
    void resolve(Resolver resolver);

    /**
     * Fulfill this {@link RacingProcedurePrerequisite} with reasonable defaults.
     */
    void fulfillWithDefault();
}
