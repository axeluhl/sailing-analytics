package com.sap.sailing.domain.abstractlog.race.state.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPassChangeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ConfirmedFinishPositioningListFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastWindFixFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ProtestTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceStatusAnalyzer.Clock;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
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
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.util.WeakIdentityHashMap;

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
    private static final WeakIdentityHashMap<RaceLog, WeakIdentityHashMap<RaceLogResolver, ReadonlyRaceState>> raceStateCache =
            new WeakIdentityHashMap<>();
    
    /**
     * Obtains a race state for the combination of {@code raceLog} and {@code raceLogResolver}. The result is
     * cache using weak references to the race log and the race log resolver for quick retrieval upon equal
     * requests, saving memory, saving listeners and thereby avoiding more synchronization issues.
     * 
     * See also bug4704.
     */
    public static ReadonlyRaceState getOrCreate(RaceLogResolver raceLogResolver, RaceLog raceLog) {
        WeakIdentityHashMap<RaceLogResolver, ReadonlyRaceState> raceStatesForRaceLog;
        ReadonlyRaceState result = null;
        synchronized (raceStateCache) {
            raceStatesForRaceLog = raceStateCache.get(raceLog);
            if (raceStatesForRaceLog == null) {
                raceStatesForRaceLog = new WeakIdentityHashMap<>();
                raceStateCache.put(raceLog, raceStatesForRaceLog);
            } else {
                result = raceStatesForRaceLog.get(raceLogResolver);
            }
            if (result == null) {
                result = createInternal(raceLogResolver, raceLog);
                raceStatesForRaceLog.put(raceLogResolver, result);
            }
        }
        return result;
    }
        
        
    private static final ReadonlyRaceState createInternal(RaceLogResolver raceLogResolver, RaceLog raceLog) {
        return create(raceLogResolver, raceLog, /* forRaceLogIdentifier */null,
                Collections.<SimpleRaceLogIdentifier, ReadonlyRaceState> emptyMap());
    }

    /**
     * Creates a {@link ReadonlyRaceState}.
     * 
     * @param forRaceLogIdentifier
     *            If known, callers can specify the simple race log identifier for the race log to be wrapped by this
     *            new race state. This can help avoid endless recursions because this race state can be passed along
     *            together with the identifier so that when again along a dependent call chain a race state is required
     *            for that race log identifier, this one can be used.
     */
    public static ReadonlyRaceState create(RaceLogResolver raceLogResolver, RaceLog raceLog,
            SimpleRaceLogIdentifier forRaceLogIdentifier,
            ConfigurationLoader<RegattaConfiguration> configurationLoader,
            Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        return new ReadonlyRaceStateImpl(raceLogResolver, raceLog, forRaceLogIdentifier, new ReadonlyRacingProcedureFactory(
                        configurationLoader), dependentRaceStates);
    }

    /**
     * Creates a {@link ReadonlyRaceState} with an empty configuration ( {@link EmptyRegattaConfiguration} ).
     * 
     * @param forRaceLogIdentifier
     *            If known, callers can specify the simple race log identifier for the race log to be wrapped by this
     *            new race state. This can help avoid endless recursions because this race state can be passed along
     *            together with the identifier so that when again along a dependent call chain a race state is required
     *            for that race log identifier, this one can be used.
     */
    public static ReadonlyRaceState create(RaceLogResolver raceLogResolver, RaceLog raceLog,
            SimpleRaceLogIdentifier forRaceLogIdentifier, Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        return new ReadonlyRaceStateImpl(raceLogResolver, raceLog, forRaceLogIdentifier, new ReadonlyRacingProcedureFactory(
                        new EmptyRegattaConfiguration()), dependentRaceStates);
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
    private final ProtestTimeFinder protestTimeAnalyzer;

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
    private StartTimeFinderResult cachedStartTimeFinderResult;
    private TimePoint cachedFinishingTime;
    private TimePoint cachedFinishedTime;
    private TimeRange cachedProtest;
    private CompetitorResults cachedPositionedCompetitors;
    private CompetitorResults cachedConfirmedPositionedCompetitors;
    private CourseBase cachedCourseDesign;
    private Wind cachedWindFix;
    private RaceLogResolver raceLogResolver;

    // For dependentStartTime
    private ReadonlyRaceState raceStateToObserve;
    private RaceStateChangedListener raceStateToObserveListener;

    private ReadonlyRaceStateImpl(RaceLogResolver raceLogResolver, RaceLog raceLog,
            SimpleRaceLogIdentifier forRaceLogIdentifier, RacingProcedureFactory procedureFactory,
            Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        this(raceLogResolver, raceLog, forRaceLogIdentifier, new RaceStatusAnalyzer.StandardClock(),
                procedureFactory, dependentRaceStates, /* update */true);
    }

    /**
     * @param forRaceLogIdentifier
     *            If known, callers can specify the simple race log identifier for the race log to be wrapped by this
     *            new race state. This can help avoid endless recursions because this race state can be passed along
     *            together with the identifier so that when again along a dependent call chain a race state is required
     *            for that race log identifier, this one can be used.
     * @param update
     *            if <code>true</code>, the race state will be updated whenever the underlying <code>raceLog</code>
     *            changes.
     */
    protected ReadonlyRaceStateImpl(RaceLogResolver raceLogResolver, RaceLog raceLog,
            SimpleRaceLogIdentifier forRaceLogIdentifier, Clock analyzersClock,
            RacingProcedureFactory procedureFactory,
            final Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates, boolean update) {
        this.raceLog = raceLog;
        this.raceLogResolver = raceLogResolver;
        this.procedureFactory = procedureFactory;
        this.changedListeners = new RaceStateChangedListeners();

        this.racingProcedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        this.statusAnalyzerClock = analyzersClock;
        // status analyzer will get initialized when racing procedure is ready
        this.startTimeAnalyzer = new StartTimeFinder(raceLogResolver, raceLog);
        this.finishingTimeAnalyzer = new FinishingTimeFinder(raceLog);
        this.finishedTimeAnalyzer = new FinishedTimeFinder(raceLog);
        this.protestTimeAnalyzer = new ProtestTimeFinder(raceLog);
        this.finishPositioningListAnalyzer = new FinishPositioningListFinder(raceLog);
        this.confirmedFinishPositioningListAnalyzer = new ConfirmedFinishPositioningListFinder(raceLog);
        this.courseDesignerAnalyzer = new LastPublishedCourseDesignFinder(raceLog, /* onlyCoursesWithValidWaypointList */ false);
        this.lastWindFixAnalyzer = new LastWindFixFinder(raceLog);

        this.raceStateToObserveListener = new BaseRaceStateChangedListener() {
            @Override
            public void onStartTimeChanged(ReadonlyRaceState state) {
                update();
            }
        };

        this.cachedRacingProcedureTypeNoFallback = determineInitialProcedureType();
        if (this.cachedRacingProcedureTypeNoFallback == null
                || this.cachedRacingProcedureTypeNoFallback == RacingProcedureType.UNKNOWN) {
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
        final Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStatesAndMe;
        if (forRaceLogIdentifier != null) {
            dependentRaceStatesAndMe = new HashMap<>(dependentRaceStates);
            dependentRaceStatesAndMe.put(forRaceLogIdentifier, this);
        } else {
            dependentRaceStatesAndMe = dependentRaceStates;
        }
        recreateRacingProcedure(dependentRaceStatesAndMe);
        // Check whether the latest known StartTimeEvent is a non-dependent or dependent start time in case of a
        // dependent startTime setup listeners
        adjustObserverForRelativeStartTime(dependentRaceStatesAndMe);
    }
    
    protected ReadonlyRaceState getRaceStateToObserve() {
        return raceStateToObserve;
    }

    protected RacingProcedureType determineInitialProcedureType() {
        // Let's ensure there is a valid RacingProcedureType set, since a RaceState cannot live without a
        // RacingProcedure we need to have a fallback
        RacingProcedureType inRaceLogType = racingProcedureAnalyzer.analyze();
        return determineInitialProcedureType(inRaceLogType);
    }
    
    private RacingProcedureType determineInitialProcedureType(RacingProcedureType inRaceLogType) {
        RegattaConfiguration configuration = getConfiguration();
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
        if (cachedRacingProcedureTypeNoFallback == null
                || cachedRacingProcedureTypeNoFallback == RacingProcedureType.UNKNOWN) {
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
        return cachedStartTimeFinderResult.getStartTime();
    }
    
    @Override
    public StartTimeFinderResult getStartTimeFinderResult() {
        return cachedStartTimeFinderResult;
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
    public TimeRange getProtestTime() {
        return cachedProtest;
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
        // if a pass change or a start time-setting event, the observer relation to a race state of a
        // race on which this race state's race's start time has depended or now depends needs to be
        // re-assessed, and the observer relation needs to be established or canceled, respectively.
        if (event instanceof RaceLogDependentStartTimeEvent ||
                event instanceof RaceLogStartTimeEvent ||
                event instanceof RaceLogPassChangeEvent) {
            adjustObserverForRelativeStartTime();
        }
        update();
    }

    private void adjustObserverForRelativeStartTime() {
        adjustObserverForRelativeStartTime(Collections.<SimpleRaceLogIdentifier, ReadonlyRaceState>emptyMap());
    }
    
    private void adjustObserverForRelativeStartTime(final Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        final StartTimeFinderResult startTimeAnalysisResult = startTimeAnalyzer.analyze();
        adjustObserverForRelativeStartTime(startTimeAnalysisResult, dependentRaceStates);
    }
    
    private void adjustObserverForRelativeStartTime(final StartTimeFinderResult startTimeAnalysisResult, Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        if (startTimeAnalysisResult.isDependentStartTime()) {
            setupListenersOnDependentRace(startTimeAnalysisResult, dependentRaceStates);
        } else if (raceStateToObserve != null) {
            raceStateToObserve.removeChangedListener(raceStateToObserveListener);
            raceStateToObserve = null;
        }
    }

    private void setupListenersOnDependentRace(StartTimeFinderResult startTimeAnalysisResult, Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        assert startTimeAnalysisResult.isDependentStartTime();
        assert !Util.isEmpty(startTimeAnalysisResult.getDependingOnRaces());
        if (raceStateToObserve != null) {
            // Remove previous listeners
            raceStateToObserve.removeChangedListener(raceStateToObserveListener);
            raceStateToObserve = null;
        }
        final SimpleRaceLogIdentifier dependentOnRaceIdentifier = startTimeAnalysisResult.getDependingOnRaces().iterator().next();
        if (dependentRaceStates.containsKey(dependentOnRaceIdentifier)) {
            raceStateToObserve = dependentRaceStates.get(dependentOnRaceIdentifier);
            raceStateToObserve.addChangedListener(raceStateToObserveListener);
        } else {
            RaceLog resolvedRaceLog = raceLogResolver.resolve(dependentOnRaceIdentifier);
            if (resolvedRaceLog != null) {
                raceStateToObserve = ReadonlyRaceStateImpl.create(raceLogResolver, resolvedRaceLog,
                        dependentOnRaceIdentifier, dependentRaceStates);
                raceStateToObserve.addChangedListener(raceStateToObserveListener);
            }
        }
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

    protected void update() {
        update(/* dependentRaceStates */ Collections.<SimpleRaceLogIdentifier, ReadonlyRaceState>emptyMap());
    }
    
    protected void update(Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        RacingProcedureType type = racingProcedureAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedRacingProcedureType, type) && type != RacingProcedureType.UNKNOWN) {
            cachedRacingProcedureType = type;
            cachedRacingProcedureTypeNoFallback = determineInitialProcedureType(type);
            recreateRacingProcedure(dependentRaceStates);
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
            cachedRacingProcedureTypeNoFallback = null;
        }

        StartTimeFinderResult startTimeFinderResult = startTimeAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedStartTimeFinderResult, startTimeFinderResult)) {
            cachedStartTimeFinderResult = startTimeFinderResult;
            changedListeners.onStartTimeChanged(this);
        }
        adjustObserverForRelativeStartTime(startTimeFinderResult, dependentRaceStates);

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

        TimeRange protest = protestTimeAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedProtest, protest)) {
            cachedProtest = protest;
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

    private void recreateRacingProcedure(Map<SimpleRaceLogIdentifier, ReadonlyRaceState> dependentRaceStates) {
        if (racingProcedure != null) {
            removeChangedListener(racingProcedure);
            racingProcedure.detach();
        }
        racingProcedure = procedureFactory.createRacingProcedure(cachedRacingProcedureType, raceLog, raceLogResolver);
        racingProcedure.setStateEventScheduler(scheduler);
        addChangedListener(racingProcedure);

        statusAnalyzer = new RaceStatusAnalyzer(raceLogResolver, raceLog, statusAnalyzerClock, racingProcedure);
        // let's do an update because status might have changed with new procedure
        update(dependentRaceStates);
    }

}
