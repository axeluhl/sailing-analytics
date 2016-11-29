package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.LineStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.ConfigurableStartModeFlagRacingProcedureConfiguration;

public abstract class ConfigurableStartModeFlagRacingProcedureImpl extends BaseRacingProcedure implements ConfigurableStartModeFlagRacingProcedure {
    
    public ConfigurableStartModeFlagRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author,
            RacingProcedureConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
    }

    @Override
    protected RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> createChangedListenerContainer() {
        return new LineStartChangedListeners();
    }
    
    @Override
    protected LineStartChangedListeners getChangedListeners() {
        return (LineStartChangedListeners) super.getChangedListeners();
    }

    @Override
    public void addChangedListener(LineStartChangedListener listener) {
        getChangedListeners().add(listener);
    }
    
    @Override
    public ConfigurableStartModeFlagRacingProcedureConfiguration getConfiguration() {
        return (ConfigurableStartModeFlagRacingProcedureConfiguration) super.getConfiguration();
    }
}
