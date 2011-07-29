package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A leader-board is used to display the results of one or more {@link TrackedRace races}. It manages the competitors'
 * scores and can aggregate them, e.g., to show the overall regatta standings.
 * <p>
 * 
 * While a single {@link TrackedRace} can tell about the ranks in which according to the tracking information the
 * competitors crossed the finish line, the leader-board may overlay this information with disqualifications, changes in
 * results because the finish-line tracking was inaccurate, jury penalties and discarded results (depending on the
 * regatta rules, the worst zero, one or more races of each competitor are discarded from the aggregated points).
 * <p>
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Leaderboard {
    Iterable<TrackedRace> getRaces();

    Iterable<Competitor> getCompetitors();

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

    void addRace(TrackedRace race);
}
