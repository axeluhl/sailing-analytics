package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

/**
 * Manages a set of {@link Competitor} objects. There may be a transient implementation based on a simple cache,
 * and there may be persistent implementations.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CompetitorStore extends CompetitorFactory {
    public interface CompetitorUpdateListener {
        void competitorUpdated(Competitor competitor);
        void competitorCreated(Competitor competitor);
    }
    
    /**
     * If a valid competitor is returned and the caller has information available that could be used to update the competitor,
     * the caller must check the result of {@link #isCompetitorToUpdateDuringGetOrCreate(Competitor)}, and if <code>true</code>,
     * must call {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)} to cause an update of the
     * competitor's values.
     */
    Competitor getExistingCompetitorByIdAsString(String idAsString);
    
    /**
     * When a competitor is queried using {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)}
     * , and the competitor object for that ID already exists, it is generally returned unchanged, and the name, team
     * and boat parameters are not evaluated. This makes the data in this competitor store generally "write-once." This
     * method can be used to reset a competitor object to what a tracking provider or an external system supplies to
     * {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)}. After calling this method, the
     * next call to {@link #getOrCreateCompetitor(Serializable, String, DynamicTeam, DynamicBoat)} with
     * <code>competitor</code>'s {@link Competitor#getId() ID} will
     * {@link #updateCompetitor(String, String, String, Nationality)} the competitor in its updatable properties such as
     * the name, the sail ID and the nationality.
     */
    void allowCompetitorResetToDefaults(Competitor competitor);

    int size();

    /**
     * Removes all competitors from this store. Use with due care.
     */
    void clear();
    
    /**
     * Obtains a non-live snapshot of the list of competitors managed by this store.
     */
    Iterable<? extends Competitor> getCompetitors();
    
    void removeCompetitor(Competitor competitor);

    /**
     * Updates the competitor with {@link Competitor#getId() ID} <code>id</code> by setting the name, sail ID and nationality to
     * the values provided. Doing so will not fire any events nor will it replicate this change from a master to any replicas.
     * The calling client has to make sure that the changes applied will reach replicas and all other interested clients. It will
     * be sufficient to ensure that subsequent DTOs produced from the competitor modified will reflect the changes.<p>
     * 
     * If no competitor with the ID requested is found, the call is a no-op, doing nothing, not even throwing an exception.
     */
    Competitor updateCompetitor(String idAsString, String newName, Color newDisplayColor, String newEmail,
            String newSailId, Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag);

    void addNewCompetitors(Iterable<Competitor> competitors);

    CompetitorDTO convertToCompetitorDTO(Competitor c);
    
    /**
     * Listeners added here are notified whenever {@link #updateCompetitor(String, String, Color, String, Nationality)} is called
     * for any competitor in this store.
     */
    void addCompetitorUpdateListener(CompetitorUpdateListener listener);
    
    void removeCompetitorUpdateListener(CompetitorUpdateListener listener);
}
