package com.sap.sailing.domain.base;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface LeaderboardBase extends Named, WithQualifiedObjectIdentifier {
    /**
     * If a display name for the leaderboard has been defined,
     * this method returns it; otherwise, <code>null</code> is returned.
     */
    String getDisplayName();
    
    void addLeaderboardChangeListener(LeaderboardChangeListener listener);
    
    void removeLeaderboardChangeListener(LeaderboardChangeListener listener);

    /**
     * Gets the course areas that the races of this leaderboard are expected to be run on. This can, e.g., be used to
     * implement a filter when retrieving leaderboards from an event.
     *
     * @return the {@link CourseArea} objects on which races of this leaderboard may run; always valid, never
     *         {@code null}, but may be empty
     */
    Iterable<CourseArea> getCourseAreas();
}
