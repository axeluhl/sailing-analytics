package com.sap.sailing.domain.racelog.state.impl;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.tracking.Wind;

public class RaceStateImpl extends ReadonlyRaceStateImpl implements RaceState {
    
    private final RaceLogEventAuthor author;
    private final RaceLogEventFactory factory;

    public RaceStateImpl(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory eventFactory,
            RacingProcedureType initalRacingProcedureType, StoredRacingProceduresConfiguration configuration) {
        super(raceLog, initalRacingProcedureType, configuration);
        this.author = author;
        this.factory = eventFactory;
    }
    
    @Override
    public RacingProcedure getRacingProcedure() {
        return (RacingProcedure) super.getRacingProcedure();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RacingProcedure> T getTypedRacingProcedure() {
        return (T) getRacingProcedure();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RacingProcedure> T getTypedRacingProcedure(Class<T> clazz) {
        RacingProcedure procedure = getRacingProcedure();
        if (clazz.isAssignableFrom(procedure.getClass())) {
            return (T) procedure;
        } else {
            return null;
        }
    }
    
    @Override
    protected RacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog, RacingProceduresConfiguration configuration) {
        return RacingProcedureFactoryImpl.create(type, raceLog, author, factory, configuration);
    }

    @Override
    public RaceLogEventAuthor getAuthor() {
        return author;
    }

    @Override
    public void setRacingProcedure(TimePoint timePoint, RacingProcedureType newType) {
        raceLog.add(factory.createStartProcedureChangedEvent(timePoint, author, raceLog.getCurrentPassId(), newType));
    }

    @Override
    public RacingProcedurePrerequisite setStartTime(TimePoint now, TimePoint startTime) {
        RacingProcedurePrerequisite pre = getRacingProcedure().checkPrerequisitesForStart(startTime, now);
        if (pre != null) {
            return pre;
        } else {
            raceLog.add(factory.createStartTimeEvent(now, author, raceLog.getCurrentPassId(), startTime));
            return null;
        }
    }

    @Override
    public void setFinishingTime(TimePoint timePoint) {
        raceLog.add(factory.createRaceStatusEvent(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING));
    }

    @Override
    public void setFinishedTime(TimePoint timePoint) {
        raceLog.add(factory.createRaceStatusEvent(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHED));
    }

    @Override
    public void setProtestTime(TimePoint now, TimePoint timePoint) {
        raceLog.add(factory.createProtestStartTimeEvent(now, author, raceLog.getCurrentPassId(), timePoint));
    }

    @Override
    public void setAdvancePass(TimePoint timePoint) {
        raceLog.add(factory.createPassChangeEvent(timePoint, author, raceLog.getCurrentPassId() + 1));
    }

    @Override
    public void setAborted(TimePoint timePoint, boolean isPostponed, Flags reasonFlag) {
        Flags markerFlag = isPostponed ? Flags.AP : Flags.NOVEMBER;
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), markerFlag, reasonFlag, true));
        setAdvancePass(timePoint.plus(1));
    }

    @Override
    public void setGeneralRecall(TimePoint timePoint) {
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, true));
        setAdvancePass(timePoint);
    }

    @Override
    public void setFinishPositioningListChanged(TimePoint timePoint, CompetitorResults positionedCompetitors) {
        raceLog.add(factory.createFinishPositioningListChangedEvent(
                timePoint, author, raceLog.getCurrentPassId(), positionedCompetitors));
    }

    @Override
    public void setFinishPositioningConfirmed(TimePoint timePoint) {
        raceLog.add(factory.createFinishPositioningConfirmedEvent(
                timePoint, author, raceLog.getCurrentPassId(), getFinishPositioningList()));
    }

    @Override
    public void setCourseDesign(TimePoint timePoint, CourseBase courseDesign) {
        raceLog.add(factory.createCourseDesignChangedEvent(timePoint, author, raceLog.getCurrentPassId(), courseDesign));
    }

    @Override
    public void setWindFix(TimePoint timePoint, Wind wind) {
        raceLog.add(factory.createWindFixEvent(timePoint, author, raceLog.getCurrentPassId(), wind));
    }

}
