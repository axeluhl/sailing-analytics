package com.sap.sailing.domain.base;

import java.net.URI;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithID;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;

public interface Competitor extends NamedWithID, IsManagedByCache<SharedDomainFactory> {
    Team getTeam();
    
    /**
     * Short for {@link #getTeam()}.{@link Team#getNationality() getNationality()}.
     */
    @Connector(messageKey="Nationality", ordinal=9)
    Nationality getNationality();

    Color getColor();
    
    String getEmail();

    boolean hasEmail();

    String getShortName();

    /**
     * Returns a derived short information about a competitor depending on the information available
     * If we have a short name set on the competitor this name will be returned.
     * If no short name exist but a boat the either the sailId or the boat name will returned.
     * If all these attributes have no value null is returned.   
     */
    String getShortInfo();

    /**
     * A helper to know if the competitor has a boat attached.
     * See {@link CompetitorWithBoat}
     */
    boolean hasBoat();

    @Dimension(messageKey="SearchTag", ordinal=11)
    String getSearchTag();
    
    /**
     * an alternative flag image (per default the nation flag
     * @return the URI of the flag image
     */
    URI getFlagImage();

    /**
     * Adds a listener to this competitor. The listener is also added to the boat and the team for changes.
     * Adding a listener that is already part of this competitor's listeners set remains without effect.
     */
    void addCompetitorChangeListener(CompetitorChangeListener listener);
    
    /**
     * Removes a listener from this competitor. The listener is also removed from the boat and the team.
     * Trying to remove a listener that is not currently in the set of this competitor's listener remains
     * without effect.
     */
    void removeCompetitorChangeListener(CompetitorChangeListener listener);
    
    /**
     * The default time-on-time handicap factor for this competitor. If <code>null</code>, no time-on-time handicap
     * factor is defined for this competitor. For a simple time-on-time scheme, the factor is multiplied with the actual
     * time sailed to obtain the corrected time. For a "performance line" system, afterwards an additional
     * distance-based allowance may be subtracted from the resulting time.
     * <p>
     * 
     * A particular regatta can make specific arrangements and override this default value for this competitor in its
     * {@link RegattaLog}, accommodating for the fact that in between regattas competitors may get a new, different
     * rating, resulting in different handicaps for different regattas.
     * 
     * @see #getTimeOnDistanceAllowancePerNauticalMile
     */
    Double getTimeOnTimeFactor();
    
    /**
     * The default time-on-distance time allowance for this competitor, used in handicap regattas. If <code>null</code>,
     * no time-on-distance allowance is defined for this competitor. For a simple time-on-distance scheme, the factor is
     * multiplied with the (windward) course length and the result is subtracted from the actual time sailed to obtain
     * the corrected time. For a "performance line" system, the actual time sailed will be multiplied with the
     * {@link #getTimeOnTimeFactor() time-on-time factor} before subtracting the time-on-distance allowance.
     * <p>
     * 
     * A particular regatta can make specific arrangements and override this default value for this competitor in its
     * {@link RegattaLog}, accommodating for the fact that in between regattas competitors may get a new, different
     * rating, resulting in different handicaps for different regattas.
     * 
     * @see #getTimeOnTimeFactor
     */
    Duration getTimeOnDistanceAllowancePerNauticalMile();
}
