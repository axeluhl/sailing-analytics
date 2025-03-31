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

import java.util.List;

public abstract class ConfigurableStartModeFlagRacingProcedureImpl extends BaseRacingProcedure implements ConfigurableStartModeFlagRacingProcedure {

    Flags cachedStartmodeFlag;
    boolean startmodeFlagHasBeenSet;
    private StartModeFlagFinder startModeFlagAnalyzer;

    ConfigurableStartModeFlagRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author,
                                                 ConfigurableStartModeFlagRacingProcedureConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
        RacingProcedureTypeAnalyzer procedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        List<Flags> startModeFlags = getConfiguration().getStartModeFlags();
        if (startModeFlags == null) {
            this.startModeFlagAnalyzer = new StartModeFlagFinder(procedureAnalyzer, raceLog, getDefaultStartModeFlags());
        } else {
            Flags defaultStartMode = getDefaultStartMode();
            if (startModeFlags.isEmpty() || startModeFlags.contains(defaultStartMode)) {
                this.cachedStartmodeFlag = defaultStartMode;
            } else {
                this.cachedStartmodeFlag = startModeFlags.get(0);
            }
            this.startModeFlagAnalyzer = new StartModeFlagFinder(procedureAnalyzer, raceLog, startModeFlags);
        }
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
        if (startTime.minus(getStartPhaseStartModeUpInterval()).before(now) && !startmodeFlagHasBeenSet) {
            return new StartModePrerequisite(function, this, now, startTime);
        }
        return new NoMorePrerequisite(function);
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
        Flags startModeFlag = startModeFlagAnalyzer.analyze();
        if (startModeFlag != null && (!startModeFlag.equals(cachedStartmodeFlag) || !startmodeFlagHasBeenSet)) {
            startmodeFlagHasBeenSet = true;
            cachedStartmodeFlag = startModeFlag;
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
