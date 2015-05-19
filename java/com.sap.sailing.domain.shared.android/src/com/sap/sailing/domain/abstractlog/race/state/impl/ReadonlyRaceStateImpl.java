package com.sap.sailing.domain.abstractlog.race.state.impl;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ConfirmedFinishPositioningListFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastWindFixFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ProtestStartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderStatus;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceStatusAnalyzer.Clock;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.impl.WeakRaceLogChangedVisitor;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEventScheduler;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.ReadonlyRacingProcedureFactory;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Implementation of {@link ReadonlyRaceState}. Use the static factory methods to instantiate your race state.
 * 
 * The {@link ReadonlyRaceStateImpl} ensures that it will not change the {@link RaceLog} on its own by wrapping the
 * {@link RaceLog}. This way even automatic events triggered by the attached {@link RacingProcedure}s will never add any
 * events to the {@link RaceLog}.
 * 
 * See {@link ReadonlyRaceStateImpl#update()} for the core of the algorithm.
 */
public class ReadonlyRaceStateImpl implements ReadonlyRaceState, RaceLogChangedListener {

    /**
     * Creates a {@link ReadonlyRaceState}.
     */
    public static ReadonlyRaceState create(RaceLog raceLog,
            ConfigurationLoader<RegattaConfiguration> configurationLoader) {
        return new ReadonlyRaceStateImpl(raceLog, new ReadonlyRacingProcedureFactory(configurationLoader));
    }

    /**
     * Creates a {@link ReadonlyRaceState} with an empty configuration ( {@link EmptyRegattaConfiguration} ).
     */
    public static ReadonlyRaceState create(RaceLog raceLog) {
        return new ReadonlyRaceStateImpl(raceLog, new ReadonlyRacingProcedureFactory(new EmptyRegattaConfiguration()));
    }

    /**
     * When trying to initialize a {@link RaceStateImpl} with an initial {@link RacingProcedureType} of
     * {@link RacingProcedureType#UNKNOWN} the value of this field will be used instead.
     */
    public final static RacingProcedureType fallbackInitialProcedureType = RacingProcedureType.RRS26;

    protected final RaceLog raceLog;

    private final Clock statusAnalyzerClock;
    private final RacingProcedureFactory procedureFactory;
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

    /**
     * The cached racing procedure type. If no racing procedure type specification is found in the underlying race log,
     * a default is taken from {@link #fallbackInitialProcedureType}.
     */
    private RacingProcedureType cachedRacingProcedureType;
    
    /**
     * The result of {@link #determineInitialProcedureType()}; may be {@link RacingProcedureType#UNKNOWN} in case no
     * specification is found in the underlying race log.
     */
    private RacingProcedureType cachedRacingProcedureTypeNoFallback;
    
    private RaceLogRaceStatus cachedRaceStatus;
    private int cachedPassId;
    private TimePoint cachedStartTime;
    private TimePoint cachedFinishingTime;
    private TimePoint cachedFinishedTime;
    private TimePoint cachedProtestTime;
    private CompetitorResults cachedPositionedCompetitors;
    private CompetitorResults cachedConfirmedPositionedCompetitors;
    private CourseBase cachedCourseDesign;
    private Wind cachedWindFix;

    private ReadonlyRaceStateImpl(RaceLog raceLog, RacingProcedureFactory procedureFactory) {
        this(raceLog, new RaceStatusAnalyzer.StandardClock(), procedureFactory, /* update */ true);
    }

    /**
     * @param update
     *            if <code>true</code>, the race state will be updated whenever the underlying <code>raceLog</code>
     *            changes.
     */
    protected ReadonlyRaceStateImpl(RaceLog raceLog, Clock analyzersClock, RacingProcedureFactory procedureFactory, boolean update) {
        this.raceLog = raceLog;
        this.procedureFactory = procedureFactory;
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

        this.cachedRacingProcedureTypeNoFallback = determineInitialProcedureType();
        if (this.cachedRacingProcedureTypeNoFallback == null || this.cachedRacingProcedureTypeNoFallback == RacingProcedureType.UNKNOWN) {
            this.cachedRacingProcedureType = fallbackInitialProcedureType;
        } else {
            cachedRacingProcedureType = cachedRacingProcedureTypeNoFallback;
        }
        this.cachedRaceStatus = RaceLogRaceStatus.UNKNOWN;
        this.cachedPassId = raceLog.getCurrentPassId();
        // see resolved bug 2083: make sure the listener registration is "weak" in the sense that it is removed when
        // this race state is no longer strongly referenced
        this.raceLog.addListener(new WeakRaceLogChangedVisitor(this.raceLog, this));
        // We known that recreateRacingProcedure calls update() when done, therefore this RaceState
        // will be fully initialized after this line
        recreateRacingProcedure();
    }

    protected RacingProcedureType determineInitialProcedureType() {
        // Let's ensure there is a valid RacingProcedureType set, since a RaceState cannot live without a
        // RacingProcedure we need to have a fallback
        RegattaConfiguration configuration = getConfiguration();
        RacingProcedureType inRaceLogType = racingProcedureAnalyzer.analyze();
        if (inRaceLogType != RacingProcedureType.UNKNOWN) {
            return inRaceLogType;
        } else {
            return configuration.getDefaultRacingProcedureType();
        }
    }

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }

    @Override
    public ReadonlyRacingProcedure getRacingProcedure() {
        return racingProcedure;
    }
    
    @Override
    public ReadonlyRacingProcedure getRacingProcedureNoFallback() {
        final ReadonlyRacingProcedure result;
        if (cachedRacingProcedureTypeNoFallback == null || cachedRacingProcedureTypeNoFallback == RacingProcedureType.UNKNOWN) {
            result = null;
        } else {
            result = getRacingProcedure();
        }
        return result;
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
    public CompetitorResults getConfirmedFinishPositioningList() {
        return cachedConfirmedPositionedCompetitors;
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
    public RegattaConfiguration getConfiguration() {
        return procedureFactory.getConfiguration();
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
            // reset racing procedure to force recreate on next event!
            cachedRacingProcedureType = null;
        }

        Pair<StartTimeFinderStatus, TimePoint> startTimeFinderResult = startTimeAnalyzer.analyze();
        TimePoint startTime = null;
        if (startTimeFinderResult.getA().equals(StartTimeFinderStatus.STARTTIME_FOUND)){
            startTime = startTimeFinderResult.getB();
        } else if (startTimeFinderResult.getA().equals(StartTimeFinderStatus.STARTTIME_DEPENDENT)){
            //FIXME: fetch start time e.g. via DepedentStartTimeAnalyzer
        }
        
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

        CompetitorResults confirmedPositionedCompetitors = confirmedFinishPositioningListAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedConfirmedPositionedCompetitors, confirmedPositionedCompetitors)) {
            cachedConfirmedPositionedCompetitors = confirmedPositionedCompetitors;
            if (cachedConfirmedPositionedCompetitors != null) {
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
        racingProcedure = procedureFactory.createRacingProcedure(cachedRacingProcedureType, raceLog);
        racingProcedure.setStateEventScheduler(scheduler);
        addChangedListener(racingProcedure);

        statusAnalyzer = new RaceStatusAnalyzer(raceLog, statusAnalyzerClock, racingProcedure);
        // let's do an update because status might have changed with new procedure
        update();
    }

}
