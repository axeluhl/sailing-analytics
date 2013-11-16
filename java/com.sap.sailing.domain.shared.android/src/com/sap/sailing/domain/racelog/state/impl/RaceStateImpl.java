package com.sap.sailing.domain.racelog.state.impl;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
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
import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.domain.racelog.state.RaceStateChangedListener;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.RaceStateEventScheduler;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.tracking.Wind;

public class RaceStateImpl implements RaceState, RaceLogChangedListener {
    
    /**
     * When trying to initialize a {@link RaceStateImpl} with an initial {@link RacingProcedureType} 
     * of {@link RacingProcedureType#UNKNOWN} the value of this field will be used instead. 
     */
    private final static RacingProcedureType FallbackInitialProcedureType = RacingProcedureType.RRS26;
    
    private final RaceLog raceLog;
    private final RaceLogEventAuthor author;
    private final RaceLogEventFactory factory;
    private final StoredRacingProceduresConfiguration configuration;
    
    private final RaceState2ChangedListeners changedListeners;
    
    private RacingProcedure racingProcedure;
    private RaceStateEventScheduler scheduler;
    
    private RacingProcedureTypeAnalyzer racingProcedureAnalyzer;
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
    private int cachedPassId;
    private TimePoint cachedStartTime;
    private TimePoint cachedFinishingTime;
    private TimePoint cachedFinishedTime;
    private TimePoint cachedProtestTime;
    private CompetitorResults cachedPositionedCompetitors;
    private boolean cachedIsPositionedCompetitorsConfirmed;
    private CourseBase cachedCourseDesign;
    
    public RaceStateImpl(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory eventFactory,
            RacingProcedureType initalRacingProcedureType, StoredRacingProceduresConfiguration configuration) {
        this.raceLog = raceLog;
        this.author = author;
        this.factory = eventFactory;
        this.configuration = configuration;
        this.changedListeners = new RaceState2ChangedListeners();
        
        this.racingProcedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        this.statusAnalyzer = new RaceStatusAnalyzer(raceLog);
        this.startTimeAnalyzer = new StartTimeFinder(raceLog);
        this.finishingTimeAnalyzer = new FinishingTimeFinder(raceLog);
        this.finishedTimeAnalyzer = new FinishedTimeFinder(raceLog);
        this.protestTimeAnalyzer = new ProtestStartTimeFinder(raceLog);
        this.finishPositioningListAnalyzer = new FinishPositioningListFinder(raceLog);
        this.confirmedFinishPositioningListAnalyzer = new ConfirmedFinishPositioningListFinder(raceLog);
        this.courseDesignerAnalyzer = new LastPublishedCourseDesignFinder(raceLog);
     
        if (initalRacingProcedureType == RacingProcedureType.UNKNOWN) {
            this.cachedRacingProcedureType = FallbackInitialProcedureType;
        } else {
            this.cachedRacingProcedureType = initalRacingProcedureType;
        }
        this.cachedRaceStatus = RaceLogRaceStatus.UNKNOWN;
        this.cachedPassId = raceLog.getCurrentPassId();
        this.cachedIsPositionedCompetitorsConfirmed = false;
        
        this.raceLog.addListener(new RaceLogChangedVisitor(this));
        
        recreateRacingProcedure();
        update();
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
    public void setRacingProcedure(TimePoint timePoint, RacingProcedureType newType) {
        raceLog.add(factory.createStartProcedureChangedEvent(timePoint, author, raceLog.getCurrentPassId(), newType));
    }

    @Override
    public RacingProcedure getRacingProcedure() {
        return racingProcedure;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends RacingProcedure> T getTypedRacingProcedure() {
        return (T) getRacingProcedure();
    }

    @Override
    public void setStateEventScheduler(RaceStateEventScheduler scheduler) {
        this.scheduler = scheduler;
        if (racingProcedure != null) {
            racingProcedure.setStateEventScheduler(scheduler);
            racingProcedure.triggerStateEventScheduling(this);
        }
    }

    @Override
    public RaceLogRaceStatus getStatus() {
        return cachedRaceStatus;
    }

    @Override
    public RacingProcedurePrerequisite setStartTime(TimePoint now, TimePoint startTime) {
        RacingProcedurePrerequisite pre = racingProcedure.checkPrerequisitesForStart(startTime, now);
        if (pre != null) {
            return pre;
        } else {
            raceLog.add(factory.createStartTimeEvent(now, author, raceLog.getCurrentPassId(), startTime));
            return null;
        }
    }

    @Override
    public TimePoint getStartTime() {
        return cachedStartTime;
    }

    @Override
    public void setFinishingTime(TimePoint timePoint) {
        raceLog.add(factory.createRaceStatusEvent(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING));
    }

    @Override
    public TimePoint getFinishingTime() {
        return cachedFinishingTime;
    }

    @Override
    public void setFinishedTime(TimePoint timePoint) {
        raceLog.add(factory.createRaceStatusEvent(timePoint, author, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHED));
    }

    @Override
    public TimePoint getFinishedTime() {
        return cachedFinishedTime;
    }

    @Override
    public void setProtestTime(TimePoint now, TimePoint timePoint) {
        raceLog.add(factory.createProtestStartTimeEvent(now, author, raceLog.getCurrentPassId(), timePoint));
    }

    @Override
    public TimePoint getProtestTime() {
        return cachedProtestTime;
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
        setAdvancePass(timePoint.plus(1));
    }

    @Override
    public void setFinishPositioningListChanged(TimePoint timePoint, CompetitorResults positionedCompetitors) {
        raceLog.add(factory.createFinishPositioningListChangedEvent(
                timePoint, author, raceLog.getCurrentPassId(), positionedCompetitors));
    }

    @Override
    public CompetitorResults getFinishPositioningList() {
        return cachedPositionedCompetitors;
    }

    @Override
    public void setFinishPositioningConfirmed(TimePoint timePoint) {
        raceLog.add(factory.createFinishPositioningConfirmedEvent(
                timePoint, author, raceLog.getCurrentPassId(), getFinishPositioningList()));
    }

    @Override
    public boolean isFinishPositioningConfirmed() {
        return cachedIsPositionedCompetitorsConfirmed;
    }

    @Override
    public void setCourseDesign(TimePoint timePoint, CourseBase courseDesign) {
        raceLog.add(factory.createCourseDesignChangedEvent(timePoint, author, raceLog.getCurrentPassId(), courseDesign));
    }

    @Override
    public CourseBase getCourseDesign() {
        return cachedCourseDesign;
    }

    @Override
    public void setWindFix(TimePoint timePoint, Wind wind) {
        raceLog.add(factory.createWindFixEvent(timePoint, author, raceLog.getCurrentPassId(), wind));
    }

    @Override
    public void addChangedListener(RaceStateChangedListener listener) {
        changedListeners.add(listener);
    }

    @Override
    public void removeChangedListener(RaceStateChangedListener listener) {
        changedListeners.remove(listener);
    }

    @Override
    public void eventAdded(RaceLogEvent event) {
        update();
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        // Always ask the procedure (might be interested in START too)!
        if (racingProcedure.processStateEvent(event) || event.getEventName() == RaceStateEvents.START) {
            update();
            return true;
        }
        return false;
    }

    private void update() {
        RacingProcedureType type = racingProcedureAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedRacingProcedureType, type) && type != RacingProcedureType.UNKNOWN) {
            cachedRacingProcedureType = type;
            recreateRacingProcedure();
            changedListeners.onRacingProcedureChanged(this);
        }
        
        RaceLogRaceStatus status = statusAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedRaceStatus, status)) {
            cachedRaceStatus = status;
            changedListeners.onStatusChanged(this);
        }
        
        int passId = raceLog.getCurrentPassId();
        if (cachedPassId != passId) {
            cachedPassId = passId;
            changedListeners.onAdvancePass(this);
        }
        
        TimePoint startTime = startTimeAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedStartTime, startTime)) {
            cachedStartTime = startTime;
            changedListeners.onStartTimeChanged(this);
        }
        
        TimePoint finishingTime = finishingTimeAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedFinishingTime, finishingTime)) {
            cachedFinishingTime = finishingTime;
            changedListeners.onFinishingTimeChanged(this);
        }
        
        TimePoint finishedTime = finishedTimeAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedFinishedTime, finishedTime)) {
            cachedFinishedTime = finishedTime;
            changedListeners.onFinishedTimeChanged(this);
        }
        
        TimePoint protestTime = protestTimeAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedProtestTime, protestTime)) {
            cachedProtestTime = protestTime;
            changedListeners.onProtestTimeChanged(this);
        }
        
        CompetitorResults positionedCompetitors = finishPositioningListAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedPositionedCompetitors, positionedCompetitors)) {
            cachedPositionedCompetitors = positionedCompetitors;
            changedListeners.onFinishingPositioningsChanged(this);
        }
        
        boolean isPositionedCompetitorsConfirmed = confirmedFinishPositioningListAnalyzer.analyze() != null;
        if (cachedIsPositionedCompetitorsConfirmed != isPositionedCompetitorsConfirmed) {
            cachedIsPositionedCompetitorsConfirmed = isPositionedCompetitorsConfirmed;
            if (cachedIsPositionedCompetitorsConfirmed) {
                changedListeners.onFinishingPositionsConfirmed(this);
            }
        }
        
        CourseBase courseDesign = courseDesignerAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedCourseDesign, courseDesign)) {
            cachedCourseDesign = courseDesign;
            changedListeners.onCourseDesignChanged(this);
        }
    }

    private void recreateRacingProcedure() {
        if (racingProcedure != null) {
            removeChangedListener(racingProcedure);
            racingProcedure.detach();
        }
        RacingProceduresConfiguration loadedConfiguration = configuration.load();
        racingProcedure = RacingProcedureFactoryImpl.create(cachedRacingProcedureType, raceLog, 
                author, factory, loadedConfiguration);
        racingProcedure.setStateEventScheduler(scheduler);
        addChangedListener(racingProcedure);
        
        statusAnalyzer = new RaceStatusAnalyzer(raceLog, racingProcedure);
        // let's do another update because status might have changed
        update();
    }

}
