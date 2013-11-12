package com.sap.sailing.domain.racelog.state.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.ConfirmedFinishPositioningListFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.ProtestStartTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.domain.racelog.state.RaceState2;
import com.sap.sailing.domain.racelog.state.RaceState2ChangedListener;
import com.sap.sailing.domain.racelog.state.RacingProcedure2;
import com.sap.sailing.domain.racelog.state.RacingProcedurePrerequisite;
import com.sap.sailing.domain.tracking.Wind;

public class RaceState2Impl implements RaceState2, RaceLogChangedListener {
    
    private final RaceLog raceLog;
    private final RaceLogEventAuthor author;
    private final RaceLogEventFactory factory;
    private final RaceState2ChangedListeners changedListeners;
    
    private RacingProcedure2 racingProcedure;
    
    private RacingProcedureTypeAnalyzer racingProcedureAnalyer;
    private RaceStatusAnalyzer statusAnalyzer;
    
    private StartTimeFinder startTimeAnalyzer;
    private FinishingTimeFinder finishingTimeAnalyzer;
    private FinishedTimeFinder finishedTimeAnalyzer;
    private ProtestStartTimeFinder protestTimeAnalyzer;
    
    private FinishPositioningListFinder finishPositioningListAnalyzer;
    private ConfirmedFinishPositioningListFinder confirmedFinishPositioningListAnalyzer;
    
    private LastPublishedCourseDesignFinder courseDesignerAnalyzer;
    
    private RacingProcedureType cachedRacingProcedureType;
    private RaceLogRaceStatus cachedRaceStatus;
    
    public RaceState2Impl(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory eventFactory,
            RacingProcedureType defaultRacingProcedureType) {
        this.raceLog = raceLog;
        this.author = author;
        this.factory = eventFactory;
        this.changedListeners = new RaceState2ChangedListeners();
     
        this.cachedRacingProcedureType = defaultRacingProcedureType;
        this.cachedRaceStatus = RaceLogRaceStatus.UNKNOWN;
        
        this.raceLog.addListener(new RaceLogChangedVisitor(this));
    }

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }
    
    @Override
    public RaceLogEventAuthor getAuthor() {
        return author;
    }

    @Override
    public void setRacingProcedure(RacingProcedureType newType) {
        if (racingProcedure == null || !newType.equals(racingProcedureAnalyer.analyze())) {
            raceLog.add(factory.createStartProcedureChangedEvent(MillisecondsTimePoint.now(), author, raceLog.getCurrentPassId(), newType));
        }
    }

    @Override
    public RacingProcedure2 getRacingProcedure() {
        return racingProcedure;
    }

    @Override
    public RaceLogRaceStatus getStatus() {
        return cachedRaceStatus;
    }

    @Override
    public RacingProcedurePrerequisite setStartTime(TimePoint startTime) {
        RacingProcedurePrerequisite pre = racingProcedure.checkPrerequisitesForStart(startTime);
        if (pre != null) {
            return pre;
        } else {
            raceLog.add(factory.createStartTimeEvent(MillisecondsTimePoint.now(), author, raceLog.getCurrentPassId(), startTime));
            return null;
        }
    }

    @Override
    public TimePoint getStartTime() {
        return startTimeAnalyzer.analyze();
    }

    @Override
    public void setFinishingTime(TimePoint timePoint) {
        // set finishing time
    }

    @Override
    public TimePoint getFinishingTime() {
        return finishingTimeAnalyzer.analyze();
    }

    @Override
    public void setFinishedTime(TimePoint timePoint) {
        // set finished time
    }

    @Override
    public TimePoint getFinishedTime() {
        return finishedTimeAnalyzer.analyze();
    }

    @Override
    public void setProtestTime(TimePoint timePoint) {
        // set protest time
    }

    @Override
    public TimePoint getProtestTime() {
        return protestTimeAnalyzer.analyze();
    }

    @Override
    public void setAborted(TimePoint timePoint, boolean isPostponed) {
        // set aborted
    }

    @Override
    public void setGeneralRecall(TimePoint timePoint) {
        // set general recall
    }

    @Override
    public void setFinishPositioningListChanged(
            List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        // set finishing positions
    }

    @Override
    public List<Triple<Serializable, String, MaxPointsReason>> getFinishPositioningList() {
        return finishPositioningListAnalyzer.analyze();
    }

    @Override
    public void setFinishPositioningConfirmed() {
        // set confirmed event
    }

    @Override
    public boolean isFinishPositioningConfirmed() {
        return confirmedFinishPositioningListAnalyzer.analyze() != null;
    }

    @Override
    public void setCourseDesign(CourseBase courseDesign) {
        // set course design

    }

    @Override
    public CourseBase getCourseDesign() {
        return courseDesignerAnalyzer.analyze();
    }

    @Override
    public void setWindFix(Wind wind) {
        // set wind fix
    }

    @Override
    public void addChangedListener(RaceState2ChangedListener listener) {
        changedListeners.add(listener);
    }

    @Override
    public void removeChangedListener(RaceState2ChangedListener listener) {
        changedListeners.remove(listener);
    }

    @Override
    public void eventAdded(RaceLogEvent event) {
        RacingProcedureType type = racingProcedureAnalyer.analyze();
        if (!cachedRacingProcedureType.equals(type)) {
            cachedRacingProcedureType = type;
            createRacingProcedure();
            changedListeners.onRacingProcedureChanged(this);
        }
        
        RaceLogRaceStatus status = statusAnalyzer.analyze();
        if (!cachedRaceStatus.equals(status)) {
            cachedRaceStatus = status;
            changedListeners.onStatusChanged(this);
        }
    }

    private void createRacingProcedure() {
        this.racingProcedure = new RRS26RacingProcedure(raceLog);
    }

}
