package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Renamable;

/**
 * A leaderboard that allows its clients to flexibly modify the race columns arranged in this leaderboard without the
 * need to adhere to the constraints of a {@link Regatta} with its {@link Series} and {@link Fleet}s.
 * <p>
 * 
 * A leaderboard can be renamed. If a leaderboard is managed in a structure that keys leaderboards by name, that
 * structure's rules have to be obeyed to ensure the structure's consistency. For example,
 * <code>RacingEventService</code> has a <code>renameLeaderboard</code> method that ensures the internal structure's
 * consistency and invokes this method.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface FlexibleLeaderboard extends LeaderboardThatHasRegattaLike, Renamable, IsRegattaLike {
    /**
     * Moves the column with the name <code>name</code> up. 
     * @param name The name of the column to move.
     */
    void moveRaceColumnUp(String name);
    
    /**
     * Moves the column with the name <code>name</code> down. 
     * @param name The name of the column to move.
     */
    void moveRaceColumnDown(String name);

    /**
     * Adds a new {@link RaceColumn} that has no {@link TrackedRace} associated yet to this leaderboard. The default
     * fleet will be used.
     * 
     * @param name
     *            the name for the new race column such that none of the columns in {@link #getRaceColumns()} has that
     *            name yet; otherwise, an message will be logged, no race column will be added and the existing column
     *            will be returned for robustness reasons
     * @param medalRace
     *            tells if the column to add represents a medal race which has double score and cannot be discarded
     * @return the race column in the leaderboard used to represent the tracked <code>race</code>
     */
    FlexibleRaceColumn addRaceColumn(String name, boolean medalRace);
    
    /**
     * Adds a tracked race to this leaderboard. If a {@link RaceColumn} with name <code>columnName</code> already exists
     * in this leaderboard, <code>race</code> is {@link RaceColumn#setTrackedRace(Fleet, TrackedRace) set as its tracked
     * race} and <code>medalRace</code> is ignored. Otherwise, a new {@link RaceColumn} column, with <code>race</code>
     * as its tracked race, is created and added to this leaderboard. The default fleet will be used.
     * 
     * @param medalRace
     *            tells if the column to add represents a medal race which has double score and cannot be discarded;
     *            ignored if the column named <code>columnName</code> already exists
     * 
     * @return the race column in the leaderboard used to represent the tracked <code>race</code>
     */
    FlexibleRaceColumn addRace(TrackedRace race, String columnName, boolean medalRace);

    void removeRaceColumn(String columnName);

    void updateIsMedalRace(String raceName, boolean isMedalRace);

    /**
     * Sets the default {@link CourseArea} of this leaderboard.
     * @param newCourseArea the {@link CourseArea} to be set.
     */
    void setDefaultCourseArea(CourseArea newCourseArea);
}
