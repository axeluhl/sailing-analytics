package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;

/**
 * Interface holding configuration options common to all {@link RacingProcedure}s.
 */
public interface RacingProcedureConfiguration extends Serializable {

    /**
     * Gets the class flag to be used by the {@link RacingProcedure}.
     */
    Flags getClassFlag();

    /**
     * Indicates whether the {@link RacingProcedure} should allow individual recall or not.
     */
    Boolean hasIndividualRecall();

    /**
     * Merges this {@link RacingProcedureConfiguration} with the passed configuration, returning a copy with all
     * non-null fields of the passed configuration set to the passed configuration's value.
     * 
     * @return a merged copy.
     */
    RacingProcedureConfiguration merge(RacingProcedureConfiguration update);

    /**
     * May/shall the result entry control be used to capture results? If not, only the
     * photo feature for the hardcopy will be availble. Otherwise, a rank editor is offered
     * in the app which submits the score updates which then will be applied to the leaderboard
     * immediately.
     */
    Boolean isResultEntryEnabled();

}
