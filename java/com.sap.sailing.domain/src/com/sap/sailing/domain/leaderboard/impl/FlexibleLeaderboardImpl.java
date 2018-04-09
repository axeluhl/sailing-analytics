package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.AbstractRaceExecutionOrderProvider;
import com.sap.sailing.domain.base.impl.RegattaLogEventAdditionForwarder;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.regattalike.BaseRegattaLikeImpl;
import com.sap.sailing.domain.regattalike.FlexibleLeaderboardAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalike.RegattaLikeListener;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;

/**
 * A leaderboard implementation that allows users to flexibly configure which columns exist. No constraints need to be
 * observed regarding the columns belonging to the same regatta or even boat class.
 * <p>
 * 
 * The flexible leaderboard listens as {@link RaceColumnListener} on all its {@link RaceColumn}s and forwards all events
 * to all {@link RaceColumnListener}s subscribed with this leaderboard.
 * <p>
 * 
 * This class also implements the {@link IsRegattaLike} interface, emulating several aspects that otherwise would be
 * found on a {@link Regatta} that is modeled properly with its series and fleets. In particular, the
 * {@link IsRegattaLike} interface requires this leaderboard to provide a {@link RegattaLog} and to grant access to its
 * {@link RaceColumn}s by name. A {@link BaseRegattaLikeImpl} is used as a delegate to implement the {@link IsRegattaLike}
 * interface.<p>
 * 
 * {@link RaceColumnListener}s will be {@link RaceColumnListener#regattaLogEventAdded(RegattaLogEvent) notified} about events
 * added to the {@link RegattaLog} that belongs to this leaderboard.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class FlexibleLeaderboardImpl extends AbstractLeaderboardImpl implements FlexibleLeaderboard {
    private static Logger logger = Logger.getLogger(FlexibleLeaderboardImpl.class.getName());

    protected static final DefaultFleetImpl defaultFleet = new DefaultFleetImpl();
    private static final long serialVersionUID = -5708971849158747846L;
    private final List<FlexibleRaceColumn> races;
    private final ScoringScheme scoringScheme;
    private String name;
    private transient RaceLogStore raceLogStore;
    private CourseArea courseArea;
    private RaceExecutionOrderProvider raceExecutionOrderProvider;
    
    /**
     * @see RegattaLog for the reason why the leaderboard manages a {@code RegattaLog}
     */
    private final IsRegattaLike regattaLikeHelper;

    public FlexibleLeaderboardImpl(String name, ThresholdBasedResultDiscardingRule resultDiscardingRule,
            ScoringScheme scoringScheme, CourseArea courseArea) {
        this(EmptyRaceLogStore.INSTANCE, EmptyRegattaLogStore.INSTANCE,
                name, resultDiscardingRule, scoringScheme, courseArea);
    }

    public FlexibleLeaderboardImpl(RaceLogStore raceLogStore, RegattaLogStore regattaLogStore,
            String name, ThresholdBasedResultDiscardingRule resultDiscardingRule,
            ScoringScheme scoringScheme, CourseArea courseArea) {
        super(resultDiscardingRule);
        this.scoringScheme = scoringScheme;
        if (name == null) {
            throw new IllegalArgumentException("A leaderboard's name must not be null");
        }
        this.name = name;
        this.races = new ArrayList<>();
        this.raceLogStore = raceLogStore;
        this.courseArea = courseArea;
        this.regattaLikeHelper = new BaseRegattaLikeImpl(new FlexibleLeaderboardAsRegattaLikeIdentifier(this), regattaLogStore) {
            private static final long serialVersionUID = 4082392360832548953L;

            @Override
            public RaceColumn getRaceColumnByName(String raceColumnName) {
                return getRaceColumnByName(raceColumnName);
            }

            @Override
            public void setFleetsCanRunInParallelToTrue() {
                // no need to do anything; a FlexibleLeaderboard only has one (default) fleet
            }
        };
        this.regattaLikeHelper.addListener(new RegattaLogEventAdditionForwarder(getRaceColumnListeners()));
        this.raceExecutionOrderProvider = new RaceExecutionOrderCache();
    }
    
    /**
     * Deserialization has to be maintained in lock-step with {@link #writeObject(ObjectOutputStream) serialization}.
     * When de-serializing, a possibly remote {@link #raceLogStore} is ignored because it is transient. Instead, an
     * {@link EmptyRaceLogStore} is used for the de-serialized instance. A new {@link RaceLogInformation} is
     * assembled for this empty race log store and applied to all columns. 
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        raceLogStore = EmptyRaceLogStore.INSTANCE;
        for (RaceColumn column : getRaceColumns()) {
            column.setRaceLogInformation(raceLogStore, new FlexibleLeaderboardAsRegattaLikeIdentifier(this));
        }
        regattaLikeHelper.addListener(new RegattaLogEventAdditionForwarder(getRaceColumnListeners()));
    }
    
    protected Object readResolve() throws ObjectStreamException {
        raceExecutionOrderProvider.triggerUpdate();
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param newName must not be <code>null</code>
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("A leaderboard's name must not be null");
        }
        final String oldName = this.name;
        this.name = newName;
        notifyLeaderboardChangeListeners(listener->{
            try {
                listener.nameChanged(oldName, newName);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception trying to notify listener "+listener+" about the name of leaderboard "+
                        getName()+" changing from "+oldName+" to "+newName, e);
            }
        });
    }

    @Override
    public FlexibleRaceColumn addRace(TrackedRace race, String columnName, boolean medalRace) {
        FlexibleRaceColumn column = addRaceColumn(columnName, medalRace, /* logAlreadyExistingColumn */false);
        column.setTrackedRace(defaultFleet, race); // triggers listeners because this object was registered above as
                                                   // race column listener on the column
        return column;
    }

    @Override
    public FlexibleRaceColumn addRaceColumn(String name, boolean medalRace) {
        return addRaceColumn(name, medalRace, /* logAlreadyExistingColumn */true);
    }
    
    private FlexibleRaceColumn addRaceColumn(String name, boolean medalRace, boolean logAlreadyExistingColumn) {
        FlexibleRaceColumn column = getRaceColumnByName(name);
        if (column != null) {
            if (logAlreadyExistingColumn) {
                final String msg = "Trying to create race column with duplicate name " + name + " in leaderboard " + getName();
                logger.severe(msg);
            }
        } else {
            column = createRaceColumn(name, medalRace);
            column.addRaceColumnListener(this);
            races.add(column);
            column.setRaceLogInformation(raceLogStore, new FlexibleLeaderboardAsRegattaLikeIdentifier(this));
            column.setRegattaLikeHelper(regattaLikeHelper);
            getRaceColumnListeners().notifyListenersAboutRaceColumnAddedToContainer(column);
        }
        return column;
    }

    @Override
    public FlexibleRaceColumn getRaceColumnByName(String columnName) {
        return (FlexibleRaceColumn) super.getRaceColumnByName(columnName);
    }

    @Override
    public Fleet getFleet(String fleetName) {
        Fleet result;
        if (fleetName == null) {
            result = defaultFleet;
        } else {
            result = super.getFleet(fleetName);
        }
        return result;
    }

    @Override
    public void removeRaceColumn(String columnName) {
        final FlexibleRaceColumn raceColumn = getRaceColumnByName(columnName);
        if (raceColumn != null) {
            for (Fleet fleet : raceColumn.getFleets()) {
                raceLogStore.removeRaceLog(raceColumn.getRaceLogIdentifier(fleet));
                if (raceColumn.getTrackedRace(fleet) != null) {
                    raceColumn.getTrackedRace(fleet).detachRaceExecutionOrderProvider(raceExecutionOrderProvider);
                }
            }
            races.remove(raceColumn);
            getRaceColumnListeners().notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
            raceColumn.removeRaceColumnListener(this);
        }
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        final Iterable<RaceColumn> result;
        if (races != null) {
            result = Collections.unmodifiableCollection(new ArrayList<RaceColumn>(races));
        } else {
            result = null;
        }
        return result;
    }

    protected RaceColumnImpl createRaceColumn(String column, boolean medalRace) {
        return new RaceColumnImpl(column, medalRace, raceExecutionOrderProvider);
    }

    protected Iterable<Fleet> turnNullOrEmptyFleetsIntoDefaultFleet(Fleet... fleets) {
        Iterable<Fleet> theFleets;
        if (fleets == null || fleets.length == 0) {
            Fleet defaultfleetCasted = defaultFleet;
            theFleets = Collections.singleton(defaultfleetCasted);
        } else {
            theFleets = Arrays.asList(fleets);
        }
        return theFleets;
    }

    @Override
    public void moveRaceColumnUp(String name) {
        FlexibleRaceColumn race = null;
        for (FlexibleRaceColumn r : races) {
            if (r.getName().equals(name)) {
                race = r;
            }
        }
        if (race != null) {
            int index = 0;
            index = races.lastIndexOf(race);
            index--;
            if (index >= 0) {
                races.remove(race);
                races.add(index, race);
                getRaceColumnListeners().notifyListenersAboutRaceColumnMoved(race, index);
            }
        }
    }

    @Override
    public void moveRaceColumnDown(String name) {
        FlexibleRaceColumn race = null;
        for (FlexibleRaceColumn r : races) {
            if (r.getName().equals(name)) {
                race = r;
            }
        }
        if (race != null) {
            int index = 0;
            index = races.lastIndexOf(race);
            if (index != -1) {
                index++;
                if (index < races.size()) {
                    races.remove(race);
                    races.add(index, race);
                    getRaceColumnListeners().notifyListenersAboutRaceColumnMoved(race, index);
                }
            }
        }
    }

    @Override
    public void updateIsMedalRace(String raceName, boolean isMedalRace) {
        FlexibleRaceColumn race = null;
        for (FlexibleRaceColumn r : races) {
            if (r.getName().equals(raceName))
                race = r;
        }
        if (race != null) {
            race.setIsMedalRace(isMedalRace);
        }
    }

    @Override
    public ScoringScheme getScoringScheme() {
        return scoringScheme;
    }

    @Override
    public CourseArea getDefaultCourseArea() {
        return courseArea;
    }

    @Override
    public void setDefaultCourseArea(CourseArea newCourseArea) {
        this.courseArea = newCourseArea;
    }
    
    @Override
    public IsRegattaLike getRegattaLike() {
        return regattaLikeHelper;
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

    private class RaceExecutionOrderCache extends AbstractRaceExecutionOrderProvider {
        private static final long serialVersionUID = 652833386555762661L;

        public RaceExecutionOrderCache() {
            super();
            addRaceColumnListener(this);
        }

        @Override
        protected Map<Fleet, Iterable<? extends RaceColumn>> getRaceColumnsOfSeries() {
            final Map<Fleet, Iterable<? extends RaceColumn>> result = new HashMap<>();
            result.put(FlexibleLeaderboardImpl.defaultFleet, getRaceColumns());
            return result;
        }
    }
    
    @Override
    public LeaderboardType getLeaderboardType() {
        return LeaderboardType.FlexibleLeaderboard;
    }

    @Override
    public boolean canBoatsOfCompetitorsChangePerRace() {
        return false;
    }
    
    /**
     * In addition to invoking the superclass implementation, a flexible leaderboard also
     * detaches all race logs from any tracked race currently linked to any of the race columns
     * of this leaderboard.
     */
    @Override
    public void destroy() {
        super.destroy();
        for (final RaceColumn raceColumn : getRaceColumns()) {
            for (final Fleet fleet : raceColumn.getFleets()) {
                raceColumn.setTrackedRace(fleet, null); // this will in particular detach the race log
            }
        }
    }

    @Override
    public void setFleetsCanRunInParallelToTrue() {
        // no need to do anything because a FlexibleLeaderboard only has one (default) fleet
    }
}
