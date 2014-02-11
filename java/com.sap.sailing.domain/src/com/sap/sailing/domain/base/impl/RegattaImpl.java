package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
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
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.impl.RaceLogInformationImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogOnRegattaIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.util.impl.RaceColumnListeners;

public class RegattaImpl extends NamedImpl implements Regatta, RaceColumnListener {
    private static final Logger logger = Logger.getLogger(RegattaImpl.class.getName());
    private static final long serialVersionUID = 6509564189552478869L;
    private final Set<RaceDefinition> races;
    private final BoatClass boatClass;
    private transient Set<RegattaListener> regattaListeners;
    private final Iterable<? extends Series> series;
    private final RaceColumnListeners raceColumnListeners;
    private final ScoringScheme scoringScheme;
    private final Serializable id;
    private transient RaceLogStore raceLogStore;
    
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
     * Constructs a regatta with an empty {@link RaceLogStore}.
     */
    public RegattaImpl(String baseName, BoatClass boatClass, Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme, Serializable id, CourseArea courseArea) {
        this(EmptyRaceLogStore.INSTANCE, baseName, boatClass, series, persistent, scoringScheme, id, courseArea);
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
    public RegattaImpl(RaceLogStore raceLogStore, String baseName, BoatClass boatClass, TrackedRegattaRegistry trackedRegattaRegistry, ScoringScheme scoringScheme, Serializable id, CourseArea courseArea) {
        this(raceLogStore, baseName, boatClass, Collections.singletonList(new SeriesImpl(LeaderboardNameConstants.DEFAULT_SERIES_NAME,
                /* isMedal */false, Collections
                .singletonList(new FleetImpl(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), /* race column names */new ArrayList<String>(),
                trackedRegattaRegistry)), /* persistent */false, scoringScheme, id, courseArea);
    }

    /**
     * @param series
     *            all {@link Series} in this iterable will have their {@link Series#setRegatta(Regatta) regatta set} to
     *            this new regatta.
     */
    public RegattaImpl(RaceLogStore raceLogStore, String baseName, BoatClass boatClass, Iterable<? extends Series> series, boolean persistent, ScoringScheme scoringScheme, Serializable id, CourseArea courseArea) {
        super(getDefaultName(baseName, boatClass==null?null:boatClass.getName()));
        this.id = id;
        this.raceLogStore = raceLogStore;
        races = new HashSet<RaceDefinition>();
        regattaListeners = new HashSet<RegattaListener>();
        raceColumnListeners = new RaceColumnListeners();
        this.boatClass = boatClass;
        this.series = series;
        for (Series s : series) {
            s.setRegatta(this);
            s.addRaceColumnListener(this);
            registerRaceLogsOnRaceColumns(s);
        }
        this.persistent = persistent;
        this.scoringScheme = scoringScheme;
        this.defaultCourseArea = courseArea;
        this.configuration = null;
    }

    private void registerRaceLogsOnRaceColumns(Series series) {
        for (RaceColumn raceColumn : series.getRaceColumns()) {
            setRaceLogInformationOnRaceColumn(raceColumn);
        }
    }

    private void setRaceLogInformationOnRaceColumn(RaceColumn raceColumn) {
        raceColumn.setRaceLogInformation(
                new RaceLogInformationImpl(
                    raceLogStore,
                    new RaceLogOnRegattaIdentifier(this, raceColumn.getName())));
    }

    public static String getDefaultName(String baseName, String boatClassName) {
        return baseName+(boatClassName==null?"":" ("+boatClassName+")");
    }
    
    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public String getBaseName() {
        String result;
        if (boatClass == null) {
            result = getName();
        } else {
            result = getName().substring(0, getName().length()-boatClass.getName().length()-3); // remove tralining boat class name and " (" and ")"
        }
        return result;
    }
    
    @Override
    public boolean isPersistent() {
        return persistent;
    }
    
    /**
     * When de-serializing, a possibly remote {@link #raceLogStore} is ignored because it is transient. Instead, an
     * {@link EmptyRaceLogStore} is used for the de-serialized instance.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        raceLogStore = EmptyRaceLogStore.INSTANCE;
        regattaListeners = new HashSet<RegattaListener>();
    }

    @Override
    public Iterable<? extends Series> getSeries() {
        return series;
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
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (RaceDefinition race : getAllRaces()) {
            for (Competitor c : race.getCompetitors()) {
                result.add(c);
            }
        }
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
    public void hasSplitFleetScoreChanged(RaceColumn raceColumn, boolean hasSplitFleetScore) {
        raceColumnListeners.notifyListenersAboutHasSplitFleetScoreChanged(raceColumn, hasSplitFleetScore);
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
    public CourseArea getDefaultCourseArea() {
        return defaultCourseArea;
    }

    @Override
    public void setDefaultCourseArea(CourseArea newCourseArea) {
        this.defaultCourseArea = newCourseArea;
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

}
