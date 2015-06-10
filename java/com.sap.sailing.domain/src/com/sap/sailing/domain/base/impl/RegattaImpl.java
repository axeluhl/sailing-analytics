package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.analyzing.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventFetcher;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.ranking.RankingMetricConstructor;
import com.sap.sailing.domain.regattalike.BaseRegattaLikeImpl;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.RegattaAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeListener;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.util.impl.RaceColumnListeners;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedImpl;

public class RegattaImpl extends NamedImpl implements Regatta, RaceColumnListener {

    /**
     * Used during master data import to handle connection to correct RaceLogStore
     */
    private static transient ThreadLocal<MasterDataImportInformation> ongoingMasterDataImportInformation = new ThreadLocal<MasterDataImportInformation>() {
        @Override
        protected MasterDataImportInformation initialValue() {
            return null;
        };
    };

    public static void setOngoingMasterDataImport(MasterDataImportInformation information) {
        ongoingMasterDataImportInformation.set(information);
    }

    private static final Logger logger = Logger.getLogger(RegattaImpl.class.getName());
    private static final long serialVersionUID = 6509564189552478869L;
    private Set<RaceDefinition> races;
    private final BoatClass boatClass;
    private transient Set<RegattaListener> regattaListeners;
    private List<? extends Series> series;
    private final RaceColumnListeners raceColumnListeners;
    private final ScoringScheme scoringScheme;
    private TimePoint startDate;
    private TimePoint endDate;
    private final Serializable id;
    private transient RaceLogStore raceLogStore;
    private final IsRegattaLike regattaLikeHelper;
    private final RankingMetricConstructor rankingMetricConstructor;
    
    private CourseArea defaultCourseArea;
    private RegattaConfiguration configuration;

    /**
     * Regattas may be constructed as implicit default regattas in which case they won't need to be stored
     * durably and don't contain valuable information worth being preserved; or they are constructed explicitly
     * with series and race columns in which case this data needs to be protected. This flag indicates whether
     * the data of this regatta needs to be maintained persistently.
     * 
     * @see #isPersistent
     */
    private final boolean persistent;
    
    /**
     * Defaults to <code>true</code>. See {@link Regatta#useStartTimeInference()}.
     */
    private boolean useStartTimeInference;
  
    /**
     * Constructs a regatta with an empty {@link RaceLogStore}.
     */
    public RegattaImpl(String name, BoatClass boatClass, TimePoint startDate, TimePoint endDate, Iterable<? extends Series> series, boolean persistent,
            ScoringScheme scoringScheme, Serializable id, CourseArea courseArea) {
        this(name, boatClass, startDate, endDate, series, persistent, scoringScheme, id, courseArea, OneDesignRankingMetric::new);
    }
    
    /**
     * Constructs a regatta with an empty {@link RaceLogStore}.
     */
    public RegattaImpl(String name, BoatClass boatClass, TimePoint startDate, TimePoint endDate, Iterable<? extends Series> series, boolean persistent,
            ScoringScheme scoringScheme, Serializable id, CourseArea courseArea, RankingMetricConstructor rankingMetricConstructor) {
        this(EmptyRaceLogStore.INSTANCE, EmptyRegattaLogStore.INSTANCE, name, boatClass, startDate, endDate, series, persistent,
                scoringScheme, id, courseArea, /* useStartTimeInference */ true, rankingMetricConstructor);
    }
    
    /**
     * Constructs a regatta with a single default series with empty race column list, and a single default fleet which
     * is not {@link #isPersistent() marked for persistence}.
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    public RegattaImpl(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, String name, BoatClass boatClass, TimePoint startDate, TimePoint endDate,
            TrackedRegattaRegistry trackedRegattaRegistry, ScoringScheme scoringScheme, Serializable id,
            CourseArea courseArea) {
        this(raceLogStore, regattaLogStore, name, boatClass, startDate, endDate, trackedRegattaRegistry, scoringScheme,
                id, courseArea, OneDesignRankingMetric::new);
    }

    /**
     * Constructs a regatta with a single default series with empty race column list, and a single default fleet which
     * is not {@link #isPersistent() marked for persistence}.
     * 
     * @param trackedRegattaRegistry
     *            used to find the {@link TrackedRegatta} for this column's series' {@link Series#getRegatta() regatta}
     *            in order to re-associate a {@link TrackedRace} passed to {@link #setTrackedRace(Fleet, TrackedRace)}
     *            with this column's series' {@link TrackedRegatta}, and the tracked race's {@link RaceDefinition} with
     *            this column's series {@link Regatta}, respectively. If <code>null</code>, the re-association won't be
     *            carried out.
     */
    public RegattaImpl(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore, String name, BoatClass boatClass,
            TimePoint startDate, TimePoint endDate, TrackedRegattaRegistry trackedRegattaRegistry,
            ScoringScheme scoringScheme, Serializable id, CourseArea courseArea, RankingMetricConstructor rankingMetricConstructor) {
        this(raceLogStore, regattaLogStore, name, boatClass, startDate, endDate, Collections
                .singletonList(new SeriesImpl(LeaderboardNameConstants.DEFAULT_SERIES_NAME,
                /* isMedal */false, Collections.singletonList(new FleetImpl(LeaderboardNameConstants.DEFAULT_FLEET_NAME)),
                /* race column names */new ArrayList<String>(), trackedRegattaRegistry)), /* persistent */false,
                scoringScheme, id, courseArea, /* useStartTimeInference */ true, rankingMetricConstructor);
    }

    /**
     * @param series
     *            all {@link Series} in this iterable will have their {@link Series#setRegatta(Regatta) regatta set} to
     *            this new regatta.
     * @param rankingMetricConstructor TODO
     */
    public <S extends Series> RegattaImpl(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            String name, BoatClass boatClass, TimePoint startDate, TimePoint endDate, Iterable<S> series, boolean persistent, ScoringScheme scoringScheme,
            Serializable id, CourseArea courseArea, boolean useStartTimeInference, RankingMetricConstructor rankingMetricConstructor) {
        super(name);
        this.rankingMetricConstructor = rankingMetricConstructor;
        this.useStartTimeInference = useStartTimeInference;
        this.id = id;
        this.raceLogStore = raceLogStore;
        races = new HashSet<RaceDefinition>();
        regattaListeners = new HashSet<RegattaListener>();
        raceColumnListeners = new RaceColumnListeners();
        this.boatClass = boatClass;
        this.startDate = startDate;
        this.endDate = endDate;
        List<S> seriesList = new ArrayList<S>();
        for (S s : series) {
            seriesList.add(s);
        }
        this.series = seriesList;
        for (Series s : series) {
            linkToRegattaAndConnectRaceLogsAndAddListeners(s);
        }
        this.persistent = persistent;
        this.scoringScheme = scoringScheme;
        this.defaultCourseArea = courseArea;
        this.configuration = null;
        this.regattaLikeHelper = new BaseRegattaLikeImpl(new RegattaAsRegattaLikeIdentifier(this), regattaLogStore);
    }

    @Override
    public RankingMetricConstructor getRankingMetricConstructor() {
        // if an old version was successfully de-serialized, this field may be null; default to OneDesignRankingMetric
        return rankingMetricConstructor == null ? OneDesignRankingMetric::new : rankingMetricConstructor;
    }

    private void registerRaceLogsOnRaceColumns(Series series) {
        for (RaceColumn raceColumn : series.getRaceColumns()) {
            setRaceLogInformationOnRaceColumn(raceColumn);
        }
    }

    private void setRaceLogInformationOnRaceColumn(RaceColumn raceColumn) {
        raceColumn.setRaceLogInformation(raceLogStore, new RegattaAsRegattaLikeIdentifier(this));
    }

    @Override
    public Serializable getId() {
        return id;
    }

    public static String getDefaultName(String baseName, String boatClassName) {
        return baseName+(boatClassName==null?"":" ("+boatClassName+")");
    }
    
    @Override
    public boolean isPersistent() {
        return persistent;
    }
    
    /**
     * When de-serializing, a possibly remote {@link #raceLogStore} is ignored because it is transient. Instead, an
     * {@link EmptyRaceLogStore} is used for the de-serialized instance. A new {@link RaceLogInformation} is assembled
     * for this empty race log and applied to all columns.
     * Make sure to call {@link #initializeSeriesAfterDeserialize()} after the object graph has been de-serialized.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        regattaListeners = new HashSet<RegattaListener>();
        MasterDataImportInformation masterDataImportInformation = ongoingMasterDataImportInformation.get();
        if (masterDataImportInformation != null) {
            raceLogStore = masterDataImportInformation.getRaceLogStore();
            races = new HashSet<RaceDefinition>();
        } else {
            raceLogStore = EmptyRaceLogStore.INSTANCE;
        }
    }
    
    /**
     * {@link RaceColumnListeners} may not be de-serialized (yet) when the regatta
     * is de-serialized. Do avoid re-registering empty objects most probably leading
     * to null pointer exception one need to initialize all listeners after
     * all objects have been read.
     */
    public void initializeSeriesAfterDeserialize() {
        for (Series series : getSeries()) {
            linkToRegattaAndConnectRaceLogsAndAddListeners(series);
            if (series.getRaceColumns() != null) {
                for (RaceColumnInSeries column : series.getRaceColumns()) {
                    column.setRaceLogInformation(raceLogStore, new RegattaAsRegattaLikeIdentifier(this));
                }
            } else {
                logger.warning("Race Columns were null during deserialization. This should not happen.");
            }
        }  
    }

    @Override
    public Iterable<? extends Series> getSeries() {
        return Collections.unmodifiableCollection(series);
    }
    
    @Override
    public Series getSeriesByName(String name) {
        for (Series s : getSeries()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        synchronized (races) {
            return new ArrayList<RaceDefinition>(races);
        }
    }
    
    @Override
    public RegattaIdentifier getRegattaIdentifier() {
        return new RegattaName(getName());
    }
    
    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier(RaceDefinition race) {
        return new RegattaNameAndRaceName(getName(), race.getName());
    }

    @Override
    public RaceDefinition getRaceByName(String raceName) {
        for (RaceDefinition r : getAllRaces()) {
            if (r.getName().equals(raceName)) {
                return r;
            }
        }
        return null;
    }
    
    @Override
    public void addRace(RaceDefinition race) {
        logger.info("Adding race "+race.getName()+" to regatta "+getName()+" ("+hashCode()+")");
        if (getBoatClass() != null && race.getBoatClass() != getBoatClass()) {
            throw new IllegalArgumentException("Boat class "+race.getBoatClass()+" doesn't match regatta's boat class "+getBoatClass());
        }
        synchronized (races) {
            races.add(race);
        }
        synchronized (regattaListeners) {
            for (RegattaListener l : regattaListeners) {
                l.raceAdded(this, race);
            }
        }
    }
    
    @Override
    public void removeRace(RaceDefinition race) {
        synchronized (races) {
            logger.info("Removing race "+race.getName()+" from regatta "+getName()+" ("+hashCode()+")");
            races.remove(race);
        }
        synchronized (regattaListeners) {
            for (RegattaListener l : regattaListeners) {
                l.raceRemoved(this, race);
            }
        }
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

    @Override
    public Iterable<Competitor> getAllCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (RaceDefinition race : getAllRaces()) {
            for (Competitor c : race.getCompetitors()) {
                result.add(c);
            }
        }
        for (Series series : getSeries()) {
            for (RaceColumn rc : series.getRaceColumns()) {
                for (Fleet fleet : rc.getFleets()) {
                    TrackedRace trackedRace = rc.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        Util.addAll(trackedRace.getRace().getCompetitors(), result);
                    }
                }
            }
        }
        //consider {@link RegattaLog}
        Set<Competitor> viaLog = new RegisteredCompetitorsAnalyzer<>(regattaLikeHelper.getRegattaLog()).analyze();
        result.addAll(viaLog);
        return result;
    }

    @Override
    public void addRegattaListener(RegattaListener listener) {
        synchronized (regattaListeners) {
            regattaListeners.add(listener);
        }
    }

    @Override
    public void removeRegattaListener(RegattaListener listener) {
        synchronized (regattaListeners) {
            regattaListeners.remove(listener);
        }
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        raceColumnListeners.notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        raceColumnListeners.notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }
    
    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        raceColumnListeners.notifyListenersAboutIsMedalRaceChanged(raceColumn, newIsMedalRace);
    }
    
    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
        raceColumnListeners.notifyListenersAboutIsStartsWithZeroScoreChanged(raceColumn, newIsStartsWithZeroScore);
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
        raceColumnListeners.notifyListenersAboutIsFirstColumnIsNonDiscardableCarryForwardChanged(raceColumn, firstColumnIsNonDiscardableCarryForward);
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
        raceColumnListeners.notifyListenersAboutHasSplitFleetContiguousScoringChanged(raceColumn, hasSplitFleetContiguousScoring);
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return raceColumnListeners.canAddRaceColumnToContainer(raceColumn);
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
        setRaceLogInformationOnRaceColumn(raceColumn);
        raceColumnListeners.notifyListenersAboutRaceColumnAddedToContainer(raceColumn);
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
        for (Fleet fleet : raceColumn.getFleets()) {
            RaceLogIdentifier identifier = raceColumn.getRaceLogIdentifier(fleet);
            raceLogStore.removeRaceLog(identifier);
        }
        raceColumnListeners.notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        raceColumnListeners.notifyListenersAboutRaceColumnMoved(raceColumn, newIndex);
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        raceColumnListeners.notifyListenersAboutFactorChanged(raceColumn, oldFactor, newFactor);
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        raceColumnListeners.notifyListenersAboutCompetitorDisplayNameChanged(competitor, oldDisplayName, displayName);
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule) {
        raceColumnListeners.notifyListenersAboutResultDiscardingRuleChanged(oldDiscardingRule, newDiscardingRule);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.addRaceColumnListener(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.removeRaceColumnListener(listener);
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        raceColumnListeners.notifyListenersAboutRaceLogEventAdded(raceColumn, raceLogIdentifier, event);
    }
    
    @Override
    public ScoringScheme getScoringScheme() {
        return scoringScheme;
    }

    @Override
    public TimePoint getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(TimePoint startDate) {
        this.startDate = startDate;
    }

    @Override
    public TimePoint getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(TimePoint endDate) {
        this.endDate = endDate;
    }

    @Override
    public CourseArea getDefaultCourseArea() {
        return defaultCourseArea;
    }

    @Override
    public void setDefaultCourseArea(CourseArea newCourseArea) {
        this.defaultCourseArea = newCourseArea;
    }
    
    @Override
    public void setUseStartTimeInference(boolean useStartTimeInference) {
        this.useStartTimeInference = useStartTimeInference;
    }
    
    @Override
    public RegattaConfiguration getRegattaConfiguration() {
        return configuration;
    }
    
    @Override
    public void setRegattaConfiguration(RegattaConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @return whether this regatta defines its local per-series result discarding rules; if so, any leaderboard based
     *         on the regatta has to respect this and has to use a result discarding rule implementation that
     *         keeps discards local to each series rather than spreading them across the entire leaderboard.
     */
    @Override
    public boolean definesSeriesDiscardThresholds() {
        for (Series s : series) {
            if (s.definesSeriesDiscardThresholds()) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return getId() + " " + getName() + " " + getScoringScheme().getType().name();
    }

    @Override
    public void addSeries(Series seriesToAdd) {
        Series existingSeries = getSeriesByName(seriesToAdd.getName());
        if (existingSeries == null) {
            linkToRegattaAndConnectRaceLogsAndAddListeners(seriesToAdd);
            synchronized (this.series) {
                ArrayList<Series> newSeriesList = new ArrayList<Series>();
                for (Series seriesObject : this.series) {
                    newSeriesList.add(seriesObject);
                }
                newSeriesList.add(seriesToAdd);
                this.series = newSeriesList;
            }
        }
    }

    private void linkToRegattaAndConnectRaceLogsAndAddListeners(Series seriesToAdd) {
        seriesToAdd.setRegatta(this);
        seriesToAdd.addRaceColumnListener(this);
        registerRaceLogsOnRaceColumns(seriesToAdd);
    }

    @Override
    public void removeSeries(Series series) {
        Series existingSeries = getSeriesByName(series.getName());
        if (existingSeries != null) {
            final List<RaceColumnInSeries> raceColumns = new ArrayList<RaceColumnInSeries>();
            Util.addAll(series.getRaceColumns(), raceColumns);
            for (RaceColumn column : raceColumns) {
                for (Fleet fleet : column.getFleets()) {
                    column.removeRaceIdentifier(fleet);
                }
                series.removeRaceColumn(column.getName());
            }
            series.removeRaceColumnListener(this);
            synchronized (this.series) {
                ArrayList<Series> newSeriesList = new ArrayList<Series>();
                for (Series seriesObject : this.series) {
                    if (!seriesObject.getName().equals(series.getName())) {
                        newSeriesList.add(seriesObject);
                    }
                }
                this.series = newSeriesList;
            }
        }   
    }

    @Override
    public boolean useStartTimeInference() {
        return useStartTimeInference;
    }

    @Override
    public RegattaLog getRegattaLog() {
        return regattaLikeHelper.getRegattaLog();
    }

    @Override
    public RegattaLikeIdentifier getRegattaLikeIdentifier() {
        return regattaLikeHelper.getRegattaLikeIdentifier();
    }

    @Override
    public void addListener(RegattaLikeListener listener) {
        regattaLikeHelper.addListener(listener);
    }

    @Override
    public void removeListener(RegattaLikeListener listener) {
        regattaLikeHelper.removeListener(listener);
    }
    
    @Override
    public Double getTimeOnTimeFactor(Competitor competitor) {
        return regattaLikeHelper.getTimeOnTimeFactor(competitor);
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile(Competitor competitor) {
        return regattaLikeHelper.getTimeOnDistanceAllowancePerNauticalMile(competitor);
    }

    public void adjustEventToRegattaAssociation(EventFetcher eventFetcher) {
        CourseArea defaultCourseArea = getDefaultCourseArea();
        for (Event event : eventFetcher.getAllEvents()) {
            event.removeRegatta(this);
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                if (defaultCourseArea != null && courseArea.getId().equals(defaultCourseArea.getId())) {
                    event.addRegatta(this);
                }
            }
        }

    }
}
