package com.sap.sailing.domain.racelog.state.racingprocedure.rrs26;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sse.common.TimePoint;

public interface RRS26RacingProcedure extends ReadonlyRRS26RacingProcedure, RacingProcedure  {

    void setStartModeFlag(TimePoint timePoint, Flags startMode);
}
