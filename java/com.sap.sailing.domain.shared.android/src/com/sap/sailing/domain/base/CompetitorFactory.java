package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

/**
 * Base interface for classes managing a set of {@link Competitor} objects.
 */
public interface CompetitorFactory {
    /**
     * If a valid competitor is returned and the caller has information available that could be used to update the competitor,
     * the caller must check the result of {@link #isCompetitorToUpdateDuringGetOrCreate(Competitor)}, and if <code>true</code>,
     * must call {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)} to cause an update of the
     * competitor's values.
     */
    Competitor getExistingCompetitorById(Serializable competitorId);

    /**
     * Checks if the <code>competitor</code> shall be updated from the default provided by, e.g., a tracking infrastructure.
     * Callers of {@link #getExistingCompetitorById(Serializable)} or {@link #getExistingCompetitorByIdAsString(String)}
     * must call this method in case they retrieve a valid competitor by ID and have data available that can be used to update
     * the competitor.
     */
    boolean isCompetitorToUpdateDuringGetOrCreate(Competitor result);

    Competitor getOrCreateCompetitor(Serializable competitorId, String name, Color displayColor, String email,
            URI flagImageURI, DynamicTeam team, DynamicBoat boat, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag);
}
