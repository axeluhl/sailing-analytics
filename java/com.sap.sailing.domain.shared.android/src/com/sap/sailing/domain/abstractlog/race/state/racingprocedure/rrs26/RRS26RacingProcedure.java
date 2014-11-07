package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;

public interface RRS26RacingProcedure extends ReadonlyRRS26RacingProcedure, RacingProcedure  {

    void setStartModeFlag(TimePoint timePoint, Flags startMode);
}
