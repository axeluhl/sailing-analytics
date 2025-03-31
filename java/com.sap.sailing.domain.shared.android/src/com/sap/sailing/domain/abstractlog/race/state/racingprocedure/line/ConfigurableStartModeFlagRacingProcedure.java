package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import java.util.List;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.ConfigurableStartModeFlagRacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sse.common.TimePoint;

/**
 * A racing procedure for the typical line start as described in the 'Racing Rules of Sailing' (RRS)
 * published by the World Sailing association.
 * @author Frank
 *
 */
public interface ConfigurableStartModeFlagRacingProcedure extends RacingProcedure  {

    void addChangedListener(LineStartChangedListener listener);
    
    ConfigurableStartModeFlagRacingProcedureConfiguration getConfiguration();
    
    Flags getStartModeFlag();
    
    Flags getDefaultStartMode();
    
    List<Flags> getDefaultStartModeFlags();
    
    void setStartModeFlag(TimePoint timePoint, Flags startMode);
}
