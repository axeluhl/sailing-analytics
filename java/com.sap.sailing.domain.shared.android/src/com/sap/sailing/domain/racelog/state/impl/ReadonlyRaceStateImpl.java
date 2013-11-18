package com.sap.sailing.domain.racelog.state.impl;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.ConfirmedFinishPositioningListFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastWindFixFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.ProtestStartTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer.Clock;
import com.sap.sailing.domain.racelog.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.domain.racelog.state.RaceStateChangedListener;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.RaceStateEventScheduler;
import com.sap.sailing.domain.racelog.state.ReadonlyRaceState;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.tracking.Wind;

public class ReadonlyRaceStateImpl implements ReadonlyRaceState, RaceLogChangedListener {
    
    /**
     * When trying to initialize a {@link RaceStateImpl} with an initial {@link RacingProcedureType} 
     * of {@link RacingProcedureType#UNKNOWN} the value of this field will be used instead. 
     */
    private final static RacingProcedureType FallbackInitialProcedureType = RacingProcedureType.RRS26;
    
    protected final RaceLog raceLog;

    private final Clock statusAnalyzerClock;
    private final StoredRacingProceduresConfiguration configuration;
    private final RaceStateChangedListeners changedListeners;
    
    
    private ReadonlyRacingProcedure racingProcedure;
    private RaceStateEventScheduler scheduler;

    /**
     * We need one for each change in {@link RacingProcedureType}.
     */
    private RaceStatusAnalyzer statusAnalyzer;
    private final RacingProcedureTypeAnalyzer racingProcedureAnalyzer;
    
    private final StartTimeFinder startTimeAnalyzer;
    private final FinishingTimeFinder finishingTimeAnalyzer;
    private final FinishedTimeFinder finishedTimeAnalyzer;
    private final ProtestStartTimeFinder protestTimeAnalyzer;
    
    private final FinishPositioningListFinder finishPositioningListAnalyzer;
    private final ConfirmedFinishPositioningListFinder confirmedFinishPositioningListAnalyzer;
    
    private final LastPublishedCourseDesignFinder courseDesignerAnalyzer;
    private final LastWindFixFinder lastWindFixAnalyzer;
    
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
    private Wind cachedWindFix;
    
    public ReadonlyRaceStateImpl(RaceLog raceLog, StoredRacingProceduresConfiguration configuration) {
        this(raceLog, RacingProcedureType.UNKNOWN, new RaceStatusAnalyzer.StandardClock(), configuration);
    }
    
    public ReadonlyRaceStateImpl(RaceLog raceLog, RacingProcedureType initalRacingProcedureType, Clock analyzersClock, 
            StoredRacingProceduresConfiguration configuration) {
        this.raceLog = raceLog;
        this.configuration = configuration;
        this.changedListeners = new RaceStateChangedListeners();
        
        this.racingProcedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        this.statusAnalyzerClock = analyzersClock;
        // status analyzer will get initialized when racing procedure is ready
        this.startTimeAnalyzer = new StartTimeFinder(raceLog);
        this.finishingTimeAnalyzer = new FinishingTimeFinder(raceLog);
        this.finishedTimeAnalyzer = new FinishedTimeFinder(raceLog);
        this.protestTimeAnalyzer = new ProtestStartTimeFinder(raceLog);
        this.finishPositioningListAnalyzer = new FinishPositioningListFinder(raceLog);
        this.confirmedFinishPositioningListAnalyzer = new ConfirmedFinishPositioningListFinder(raceLog);
        this.courseDesignerAnalyzer = new LastPublishedCourseDesignFinder(raceLog);
        this.lastWindFixAnalyzer = new LastWindFixFinder(raceLog);
     
        // Let's ensure there is a valid RacingProcedureType set, since a RaceState cannot live without a
        // RacingProcedure we need to have a fallback
        if (initalRacingProcedureType == null || initalRacingProcedureType == RacingProcedureType.UNKNOWN) {
            this.cachedRacingProcedureType = FallbackInitialProcedureType;
        } else {
            this.cachedRacingProcedureType = initalRacingProcedureType;
        }
        this.cachedRaceStatus = RaceLogRaceStatus.UNKNOWN;
        this.cachedPassId = raceLog.getCurrentPassId();
        this.cachedIsPositionedCompetitorsConfirmed = false;
        
        this.raceLog.addListener(new RaceLogChangedVisitor(this));
        
        // We known that recreateRacingProcedure calls update() when done, therefore this RaceState
        // will be fully initialized after this line
        recreateRacingProcedure();
    }

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }

    @Override
    public ReadonlyRacingProcedure getRacingProcedure() {
        return racingProcedure;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ReadonlyRacingProcedure> T getTypedReadonlyRacingProcedure() {
        return (T) getRacingProcedure();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ReadonlyRacingProcedure> T getTypedReadonlyRacingProcedure(Class<T> clazz) {
        ReadonlyRacingProcedure procedure = getRacingProcedure();
        if (clazz.isAssignableFrom(procedure.getClass())) {
            return (T) procedure;
        } else {
            return null;
        }
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
    public TimePoint getStartTime() {
        return cachedStartTime;
    }

    @Override
    public TimePoint getFinishingTime() {
        return cachedFinishingTime;
    }

    @Override
    public TimePoint getFinishedTime() {
        return cachedFinishedTime;
    }

    @Override
    public TimePoint getProtestTime() {
        return cachedProtestTime;
    }

    @Override
    public CompetitorResults getFinishPositioningList() {
        return cachedPositionedCompetitors;
    }

    @Override
    public boolean isFinishPositioningConfirmed() {
        return cachedIsPositionedCompetitorsConfirmed;
    }

    @Override
    public CourseBase getCourseDesign() {
        return cachedCourseDesign;
    }

    @Override
    public Wind getWindFix() {
        return cachedWindFix;
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
        
        Wind windFix = lastWindFixAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedWindFix, windFix)) {
            cachedWindFix = windFix;
            changedListeners.onWindFixChanged(this);
        }
    }

    private void recreateRacingProcedure() {
        if (racingProcedure != null) {
            removeChangedListener(racingProcedure);
            racingProcedure.detach();
        }
        RacingProceduresConfiguration loadedConfiguration = configuration.load();
        racingProcedure = createRacingProcedure(cachedRacingProcedureType, raceLog, loadedConfiguration);
        racingProcedure.setStateEventScheduler(scheduler);
        addChangedListener(racingProcedure);
        
        statusAnalyzer = new RaceStatusAnalyzer(raceLog, statusAnalyzerClock, racingProcedure);
        // let's do an update because status might have changed with new procedure
        update();
    }
    
    protected ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog, RacingProceduresConfiguration configuration) {
        return RacingProcedureFactoryImpl.createReadonly(cachedRacingProcedureType, raceLog, configuration);
    }

}
