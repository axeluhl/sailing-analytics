package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
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
     * must call {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam...)} to cause an update of the
     * competitor's values.
     */
    Competitor getExistingCompetitorById(Serializable competitorId);

    CompetitorWithBoat getExistingCompetitorWithBoatById(Serializable competitorId);

    /**
     * Checks if the <code>competitor</code> shall be updated from the default provided by, e.g., a tracking infrastructure.
     * Callers of {@link #getExistingCompetitorById(Serializable)} or {@link #getExistingCompetitorByIdAsString(String)}
     * must call this method in case they retrieve a valid competitor by ID and have data available that can be used to update
     * the competitor.
     */
    boolean isCompetitorToUpdateDuringGetOrCreate(Competitor result);

    DynamicCompetitor getOrCreateCompetitor(Serializable competitorId, String name, String shortName, Color displayColor, String email,
            URI flagImageURI, DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag);
    
    /**
     * Updates only the competitor fields; the {@code boat} parameter is used only when the competitor needs to be
     * created because it doesn't exist yet. In this case the boat is assigned to the new competitor and stored in the
     * competitor and boat store. If changes to an existing competitor's {@link Boat} object shall be stored, use
     * {@link CompetitorAndBoatStore#getOrCreateBoat(Serializable, String, BoatClass, String, Color)}.
     */
    DynamicCompetitorWithBoat getOrCreateCompetitorWithBoat(Serializable competitorId, String name, String shortName, Color displayColor, String email,
            URI flagImageURI, DynamicTeam team, Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag, DynamicBoat boat);

}
