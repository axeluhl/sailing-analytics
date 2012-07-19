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
    void correctScore(String competitorName, RaceColumn raceColumn, double correctedScore);
    
    void setCarriedPoints(String competitorName, double carriedPoints);

    void setMaxPointsReason(String competitorName, RaceColumn raceColumn, MaxPointsReason maxPointsReason);

    void setDisplayName(String competitorName, String displayName);

    Leaderboard getLeaderboard();
}
