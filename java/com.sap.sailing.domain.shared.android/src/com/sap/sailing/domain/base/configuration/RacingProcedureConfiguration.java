package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;

/**
 * Interface holding configuration options common to all {@link RacingProcedure}s.
 */
public interface RacingProcedureConfiguration extends Serializable {
    
    Flags getClassFlag();
    Boolean hasInidividualRecall();

}
