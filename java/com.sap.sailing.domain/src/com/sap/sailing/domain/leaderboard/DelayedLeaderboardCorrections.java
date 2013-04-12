package com.sap.sailing.domain.leaderboard;

import java.io.Serializable;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * For a {@link Leaderboard}, captures corrections such as {@link ScoreCorrection}s or display names for competitors. It
 * does so by keying the corrections by competitor names which can work as a competitor "ID," e.g., in a persistence layer.
 * When for any of the leaderboards columns a new {@link TrackedRace} is linked, the not yet resolved corrections are matched
 * against the tracked race's competitor names and resolved. For this to work, this object needs to be registered as
 * {@link RaceColumnListener} with the object owning all {@link RaceColumn}s for the leaderboard. The type of object
 * may vary depending on the concrete leaderboard type.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface DelayedLeaderboardCorrections extends RaceColumnListener, Serializable {
    /**
     * Callback interface that can be used to receive a notification when this leaderboard corrections object has
     * successfully resolved all competitors for which it holds corrections.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    public interface LeaderboardCorrectionsResolvedListener {
        void correctionsResolved(DelayedLeaderboardCorrections delayedLeaderboardCorrections);
    }

    Leaderboard getLeaderboard();

    void correctScoreByID(Serializable competitorId, RaceColumn raceColumn, double correctedScore);
    
    /**
     * Deprecated. Use {@link #correctScoreByID(Serializable, RaceColumn, double)} instead. Remains available until all
     * databases have been migrated to the use of competitor IDs instead of competitor names.
     */
    void correctScoreByName(String competitorName, RaceColumn raceColumn, double correctedScore);
    
    void setCarriedPointsByID(Serializable competitorId, double carriedPoints);

    /**
     * Deprecated. Use {@link #setCarriedPointsByID(Serializable, double)} instead. Remains available until all databases have been
     * migrated to the use of competitor IDs instead of competitor names.
     */
    void setCarriedPointsByName(String competitorName, double carriedPoints);

    void setMaxPointsReasonByID(Serializable competitorId, RaceColumn raceColumn, MaxPointsReason maxPointsReason);

    /**
     * Deprecated. Use {@link #setMaxPointsReasonByID(Serializable, RaceColumn, MaxPointsReason)} instead. Remains
     * available until all databases have been migrated to the use of competitor IDs instead of competitor names.
     */
    void setMaxPointsReasonByName(String competitorName, RaceColumn raceColumn, MaxPointsReason maxPointsReason);

    void setDisplayNameByID(Serializable competitorId, String displayName);

    void setDisplayNameByName(String competitorName, String displayName);

    void suppressCompetitorById(Serializable escapedCompetitorId);

    /**
     * Deprecated. Use {@link #suppressCompetitorById(Serializable)} instead. Remains available until all databases have been
     * migrated to the use of competitor IDs instead of competitor names.
     */
    void suppressCompetitorByName(String competitorName);

    void addLeaderboardCorrectionsResolvedListener(LeaderboardCorrectionsResolvedListener listener);

    void removeLeaderboardCorrectionsResolvedListener(LeaderboardCorrectionsResolvedListener listener);

}
