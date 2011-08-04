package com.sap.sailing.domain.leaderboard;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Named;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util.Pair;

/**
 * A leaderboard is used to display the results of one or more {@link TrackedRace races}. It manages the competitors'
 * scores and can aggregate them, e.g., to show the overall regatta standings. In addition to the races, a "carry" column
 * may be used to carry results of races not displayed in the leaderboard into the calculations.
 * <p>
 * 
 * While a single {@link TrackedRace} can tell about the ranks in which according to the tracking information the
 * competitors crossed the finish line, the leaderboard may overlay this information with disqualifications, changes in
 * results because the finish-line tracking was inaccurate, jury penalties and discarded results (depending on the
 * regatta rules, the worst zero, one or more races of each competitor are discarded from the aggregated points).
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Leaderboard extends Named {
    /**
     * If the leaderboard is a "matrix" with the cells being defined by a competitor / race "coordinate,"
     * then this interface defines the structure of the "cells."
     * 
     * @author Axel Uhl (d043530)
     *
     */
    public interface Entry {
        int getTrackedPoints();
        int getNetPoints() throws NoWindException;
        int getTotalPoints() throws NoWindException;
        MaxPointsReason getMaxPointsReason();
        boolean isDiscarded() throws NoWindException;
    }
    
    Iterable<TrackedRace> getRaces();

    Iterable<Competitor> getCompetitors();
    
    Entry getEntry(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException;
    
    /**
     * Tells the number of points carried over from previous races not tracked by this leaderboard for
     * the <code>competitor</code>
     */
    int getCarriedPoints(Competitor competitor);

    /**
     * Shorthand for {@link TrackedRace#getRank(Competitor, com.sap.sailing.domain.base.TimePoint)} with the
     * additional logic that in case the <code>race</code> hasn't {@link TrackedRace#hasStarted(TimePoint) started}
     * yet, 0 points will be allotted to the race for all competitors.
     * 
     * @param competitor
     *            a competitor contained in the {@link #getCompetitors()} result
     * @param race
     *            a race that is contained in the {@link #getRaces()} result
     */
    int getTrackedPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException;

    /**
     * A possibly corrected number of points for the race specified. Defaults to the result of calling
     * {@link #getTrackedPoints(Competitor, TrackedRace, TimePoint)} but may be corrected by disqualifications or calls
     * by the jury for the particular race that differ from the tracking results.
     * 
     * @param competitor
     *            a competitor contained in the {@link #getCompetitors()} result
     * @param race
     *            a race that is contained in the {@link #getRaces()} result
     */
    int getNetPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException;

    /**
     * Tells if and why a competitor received maximum points for a race.
     */
    MaxPointsReason getMaxPointsReason(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException;

    /**
     * A possibly corrected number of points for the race specified. Defaults to the result of calling
     * {@link #getNetPoints(Competitor, TrackedRace, TimePoint)} but may be corrected by the regatta
     * rules for discarding results.
     * 
     * @param competitor
     *            a competitor contained in the {@link #getCompetitors()} result
     * @param race
     *            a race that is contained in the {@link #getRaces()} result
     */
    int getTotalPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException;

    boolean isDiscarded(Competitor competitor, TrackedRace race, TimePoint timePoint);

    /**
     * Adds a tracked race to this leaderboard. If a {@link RaceInLeaderboard} with name <code>columnName</code> already
     * exists in this leaderboard, <code>race</code> is {@link RaceInLeaderboard#setTrackedRace(TrackedRace) set as its
     * tracked race}. Otherwise, a new {@link RaceInLeaderboard} column, with <code>race</code> as its tracked race, is
     * created and added to this leaderboard.
     */
    void addRace(TrackedRace race, String columnName);

    /**
     * Sums up the {@link #getTotalPoints(Competitor, TrackedRace, TimePoint) total points} of <code>competitor</code>
     * across all races tracked by this leaderboard.
     */
    int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException;

    /**
     * Fetches all entries for all competitors of all races tracked by this leaderboard in one sweep. This saves some
     * computational effort compared to fetching all entries separately, particularly because all
     * {@link #isDiscarded(Competitor, TrackedRace, TimePoint) discarded races} of a competitor are computed in one
     * sweep using {@link ResultDiscardingRule#getDiscardedRaces(Competitor, Iterable, TimePoint)} only once.
     * Note that in order to get the {@link #getTotalPoints(Competitor, TimePoint) total points} for a competitor
     * for the entire leaderboard, the {@link #getCarriedPoints(Competitor) carried-over points} need to be added.
     */
    Map<Pair<Competitor, TrackedRace>, Entry> getContent(TimePoint timePoint) throws NoWindException;

    /**
     * A leaderboard can be renamed. If a leaderboard is managed in a structure that keys leaderboards by name,
     * that structure's rules have to be obeyed to ensure the structure's consistency. For example,
     * <code>RacingEventService</code> has a <code>renameLeaderboard</code> method that ensures the internal
     * structure's consistency and invokes this method.
     */
    void setName(String newName);

    /**
     * Adds a new {@link RaceInLeaderboard} that has no {@link TrackedRace} associated yet to this leaderboard.
     */
    void addRaceColumn(String name);

    /**
     * A leaderboard can carry over points from races that are not tracked by this leaderboard in detail,
     * so for which no {@link RaceInLeaderboard} column is present in this leaderboard. These scores are
     * simply added to the scores tracked by this leaderboard in the {@link #getTotalPoints(Competitor, TimePoint)}
     * method.
     */
    void setCarriedPoints(Competitor competitor, int carriedPoints);
    
    /**
     * Reverses the effect of {@link #setCarriedPoints(Competitor, int)}, i.e., afterwards, asking {@link #getCarriedPoints(Competitor)}
     * will return <code>0</code>. Furthermore, other than invoking {@link #setCarriedPoints(Competitor, int) setCarriedPoints(c, 0)},
     * this will, when executed for all competitors of this leaderboard, have {@link #hasCarriedPoints} return <code>false</code>.
     */
    void unsetCarriedPoints(Competitor competitor);
    
    /**
     * Tells if a carry-column shall be displayed. If the result is <code>false</code>, then no
     * {@link #setCarriedPoints(Competitor, int) scores are carried} into this leaderboard, and
     * only the race columns will be accumulated by the board.
     */
    boolean hasCarriedPoints();
}
