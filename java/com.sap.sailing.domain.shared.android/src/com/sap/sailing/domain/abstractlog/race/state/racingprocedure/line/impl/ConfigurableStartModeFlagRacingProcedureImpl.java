package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartModeFlagFinder;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.LineStartChangedListener;
import com.sap.sailing.domain.base.configuration.procedures.ConfigurableStartModeFlagRacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public abstract class ConfigurableStartModeFlagRacingProcedureImpl extends BaseRacingProcedure implements ConfigurableStartModeFlagRacingProcedure {
    
    protected Flags cachedStartmodeFlag;
    private boolean startmodeFlagHasBeenSet;
    private StartModeFlagFinder startModeFlagAnalyzer;

    public ConfigurableStartModeFlagRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author,
            ConfigurableStartModeFlagRacingProcedureConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
        RacingProcedureTypeAnalyzer procedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        if (configuration.getStartModeFlags() != null) {
            this.startModeFlagAnalyzer = new StartModeFlagFinder(procedureAnalyzer, raceLog, configuration.getStartModeFlags());
        } else {
            this.startModeFlagAnalyzer = new StartModeFlagFinder(procedureAnalyzer, raceLog, getDefaultStartModeFlags());
        }
        this.cachedStartmodeFlag = getDefaultStartMode();
        this.startmodeFlagHasBeenSet = false;
        update();
    }

    abstract protected Duration getStartPhaseStartModeUpInterval();

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        return now.before(startTime) && !now.before(startTime.minus(getStartPhaseStartModeUpInterval()));
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            FulfillmentFunction function) {
        if (startTime.minus(getStartPhaseStartModeUpInterval()).before(now) && !startmodeFlagHasBeenSet()) {
            return new StartModePrerequisite(function, this, now, startTime);
        }
        return new NoMorePrerequisite(function);
    }

    public boolean startmodeFlagHasBeenSet() {
        return startmodeFlagHasBeenSet;
    }

    @Override
    public void setStartModeFlag(TimePoint timePoint, Flags startMode) {
        raceLog.add(new RaceLogFlagEventImpl(timePoint, author, raceLog.getCurrentPassId(), startMode, Flags.NONE, true));
    }

    @Override
    public Flags getStartModeFlag() {
        return cachedStartmodeFlag;
    }

    @Override
    protected void update() {
        Flags startmodeFlag = startModeFlagAnalyzer.analyze();
        if (startmodeFlag != null && (!startmodeFlag.equals(cachedStartmodeFlag) || !startmodeFlagHasBeenSet)) {
            cachedStartmodeFlag = startmodeFlag;
            startmodeFlagHasBeenSet = true;
            getChangedListeners().onStartModeChanged(this);
        }
        super.update();
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
