package com.sap.sailing.domain.leaderboard;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A leaderboard is used to display the results of one or more {@link TrackedRace races}. It manages the competitors'
 * scores and can aggregate them, e.g., to show the overall regatta standings. In addition to the races, a "carry"
 * column may be used to carry results of races not displayed in the leaderboard into the calculations.
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
        /**
         * Tells if the net points have been corrected by a {@link ScoreCorrection}
         */
        boolean isNetPointsCorrected();
    }
    
    /**
     * Obtains the unique set of {@link Competitor} objects from all {@link TrackedRace}s currently linked to this
     * leaderboard.
     */
    Iterable<Competitor> getCompetitors();
    
    /**
     * Returns the first fleet found in the sequence of this leaderboard's {@link #getRaceColumns() race columns}'
     * {@link RaceColumn#getFleets() fleets} whose name equals <code>fleetName</code>. If no such fleet is found,
     * <code>null</code> is returned. If <code>fleetName</code> is <code>null</code>, the leaderboard may return
     * a default fleet if it has one, or <code>null</code> otherwise.
     */
    Fleet getFleet(String fleetName);
    
    Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException;
    
    /**
     * Tells the number of points carried over from previous races not tracked by this leaderboard for
     * the <code>competitor</code>. Returns <code>0</code> if there is no carried points definition for
     * <code>competitor</code>.
     */
    int getCarriedPoints(Competitor competitor);

    /**
     * Shorthand for {@link TrackedRace#getRank(Competitor, com.sap.sailing.domain.common.TimePoint)} with the
     * additional logic that in case the <code>race</code> hasn't {@link TrackedRace#hasStarted(TimePoint) started} yet
     * or no {@link TrackedRace} exists for <code>race</code>, 0 will be returned for all those competitors. The tracked
     * race for the correct {@link Fleet} is determined using {@link RaceColumn#getTrackedRace(Competitor)}.
     * 
     * @param competitor
     *            a competitor contained in the {@link #getCompetitors()} result
     * @param race
     *            a race that is contained in the {@link #getRaceColumns()} result
     */
    int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException;

    /**
     * A possibly corrected number of points for the race specified. Defaults to the result of calling
     * {@link #getTrackedRank(Competitor, TrackedRace, TimePoint)} but may be corrected by disqualifications or calls
     * by the jury for the particular race that differ from the tracking results.
     * 
     * @param competitor
     *            a competitor contained in the {@link #getCompetitors()} result
     * @param race
     *            a race that is contained in the {@link #getRaceColumns()} result
     */
    int getNetPoints(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException;

    /**
     * Tells if and why a competitor received "penalty" points for a race (however the scoring rules define the
     * points for such a penalty; usually, it would be a high score defined by the number of competitors plus one)
     */
    MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn race, TimePoint timePoint);

    /**
     * A possibly corrected number of points for the race specified. Defaults to the result of calling
     * {@link #getNetPoints(Competitor, TrackedRace, TimePoint)} but may be corrected by the regatta rules for
     * discarding results. If {@link #isDiscarded(Competitor, RaceColumn, TimePoint) discarded}, the points returned
     * will be 0.
     * 
     * @param competitor
     *            a competitor contained in the {@link #getCompetitors()} result
     * @param race
     *            a race that is contained in the {@link #getRaceColumns()} result
     */
    int getTotalPoints(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException;

    /**
     * Tells whether the contribution of <code>raceColumn</code> is discarded in the current leaderboard's standings for
     * <code>competitor</code>. A column representing a {@link RaceColumn#isMedalRace() medal race} cannot be discarded.
     * Neither can be a race where the competitor received a non-{@link MaxPointsReason#isDiscardable() discardable}
     * penalty or disqualification.
     */
    boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint);

    /**
     * Sums up the {@link #getTotalPoints(Competitor, TrackedRace, TimePoint) total points} of <code>competitor</code>
     * across all races tracked by this leaderboard.
     */
    int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException;
    
    /**
     * Sorts the competitors according to their ranking in the race column specified. Only competitors who have a score
     * are added to the result list. This excludes competitors whose fleet hasn't raced for the <code>raceColumn</code>
     * yet, and those where no tracked rank is known and no manual score correction was performed.
     * <p>
     * 
     * The sorting order considers this leaderboard's scoring scheme including the semantics of
     * {@link Fleet#compareTo(Fleet) ordered fleets} and {@link RaceColumn#isMedalRace() medal races}. The ordering
     * does not consider result discarding because when sorting for a race column it is of interest how the competitor
     * performed in that race and not how the score affected the overall regatta score. Therefore, it is based on
     * {@link #getNetPoints(Competitor, RaceColumn, TimePoint)} and not on
     * {@link #getTotalPoints(Competitor, RaceColumn, TimePoint)}.
     */
    List<Competitor> getCompetitorsFromBestToWorst(RaceColumn raceColumn, TimePoint timePoint) throws NoWindException;
    
    /**
     * Sorts the competitors according to the overall regatta standings, considering the sorting rules for
     * {@link Series}, {@link Fleet}s, medal races, discarding rules and score corrections.
     */
    List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint);

    /**
     * Fetches all entries for all competitors of all races tracked by this leaderboard in one sweep. This saves some
     * computational effort compared to fetching all entries separately, particularly because all
     * {@link #isDiscarded(Competitor, RaceColumn, TimePoint) discarded races} of a competitor are computed in one
     * sweep using {@link ResultDiscardingRule#getDiscardedRaceColumns(Competitor, Leaderboard, TimePoint)} only once.
     * Note that in order to get the {@link #getTotalPoints(Competitor, TimePoint) total points} for a competitor
     * for the entire leaderboard, the {@link #getCarriedPoints(Competitor) carried-over points} need to be added.
     */
    Map<Pair<Competitor, RaceColumn>, Entry> getContent(TimePoint timePoint) throws NoWindException;

    /**
     * Retrieves all race columns that were added, either by {@link #addRace(TrackedRace, String, boolean)} or
     * {@link #addRaceColumn(String, boolean)}.
     */
    Iterable<RaceColumn> getRaceColumns();
    
    /**
     * Retrieves a {@link RaceColumn race column} by the name used in calls to either {@link #addRaceColumn} or
     * {@link #addRace}. If no race column by the requested <code>name</code> exists, <code>null</code> is returned.
     */
    RaceColumn getRaceColumnByName(String name);
    
    /**
     * A leaderboard can carry over points from races that are not tracked by this leaderboard in detail,
     * so for which no {@link RaceColumn} column is present in this leaderboard. These scores are
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
    
    boolean hasCarriedPoints(Competitor competitor);

    SettableScoreCorrection getScoreCorrection();

    ThresholdBasedResultDiscardingRule getResultDiscardingRule();

    Competitor getCompetitorByName(String competitorName);
    
    void setDisplayName(Competitor competitor, String displayName);

    /**
     * If a display name different from the competitor's {@link Competitor#getName() name} has been defined,
     * this method returns it; otherwise, <code>null</code> is returned.
     */
    String getDisplayName(Competitor competitor);
    
    /**
     * Tells if the column represented by <code>raceColumn</code> shall be considered when counting the number of "races
     * so far" for discarding. Although medal races are never discarded themselves, they still count in determining the
     * number of "races so far" which is then the basis for deciding how many races may be discarded. If a leaderboard
     * has corrections for a column then that column shall be considered for discarding and counts for determining the
     * number of races so far. Also, if a tracked race is connected to the column and has started already, the column is
     * to be considered for discarding.
     */
    boolean considerForDiscarding(RaceColumn raceColumn, TimePoint timePoint);
    
    public void setResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule);

    Competitor getCompetitorByIdAsString(String idAsString);
    
    void addRaceColumnListener(RaceColumnListener listener);
    
    void removeRaceColumnListener(RaceColumnListener listener);
}
