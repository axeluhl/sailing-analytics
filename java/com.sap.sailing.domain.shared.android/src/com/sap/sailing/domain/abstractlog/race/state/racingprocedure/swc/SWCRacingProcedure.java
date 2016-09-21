package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sse.common.TimePoint;

public interface SWCRacingProcedure extends ReadonlySWCRacingProcedure, RacingProcedure  {

    void setStartModeFlag(TimePoint timePoint, Flags startMode);
}
