package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.regattalike.FlexibleLeaderboardAsRegattaLikeIdentifier;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A leaderboard implementation that allows users to flexibly configure which columns exist. No constraints need to be observed regarding
 * the columns belonging to the same regatta or even boat class.<p>
 * 
 * The flexible leaderboard listens as {@link RaceColumnListener} on all its {@link RaceColumn}s and forwards all events to
 * all {@link RaceColumnListener}s subscribed with this leaderboard.
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
    
    /**
     * @see RegattaLog for the reason why the leaderboard manages a {@code RegattaLog}
     */
    private final RegattaLog regattaLog;

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
        this.races = new ArrayList<FlexibleRaceColumn>();
        this.raceLogStore = raceLogStore;
        this.courseArea = courseArea;
        this.regattaLog = regattaLogStore.getRegattaLog(name, /*ignoreCache*/ true);
    }

    /**
     * Deserialization has to be maintained in lock-step with {@link #writeObject(ObjectOutputStream) serialization}.
     * When de-serializing, a possibly remote {@link #raceLogStore} is ignored because it is transient. Instead, an
     * {@link EmptyRaceLogStore} is used for the de-serialized instance. A new {@link RaceLogInformation} is
     * assembled for this empty race log and applied to all columns. 
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        raceLogStore = EmptyRaceLogStore.INSTANCE;
        for (RaceColumn column : getRaceColumns()) {
            column.setRaceLogInformation(raceLogStore, new FlexibleLeaderboardAsRegattaLikeIdentifier(this));
        }
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
        this.name = newName;
    }

    @Override
    public RaceColumn addRace(TrackedRace race, String columnName, boolean medalRace) {
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
            final String msg = "Trying to create race column with duplicate name "+name+" in leaderboard +"+getName();
            logger.severe(msg);
        } else {
            column = createRaceColumn(name, medalRace);
            column.addRaceColumnListener(this);
            races.add(column);
            column.setRaceLogInformation(raceLogStore, new FlexibleLeaderboardAsRegattaLikeIdentifier(this));
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
            }
            races.remove(raceColumn);
            getRaceColumnListeners().notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
            raceColumn.removeRaceColumnListener(this);
        }
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        return Collections.unmodifiableCollection(new ArrayList<RaceColumn>(races));
    }

    protected RaceColumnImpl createRaceColumn(String column, boolean medalRace) {
        return new RaceColumnImpl(column, medalRace);
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
    public RegattaLog getRegattaLog() {
        return regattaLog;
    }
}
