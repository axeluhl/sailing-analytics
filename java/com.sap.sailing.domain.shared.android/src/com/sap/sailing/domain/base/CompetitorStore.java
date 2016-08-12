package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.net.URI;

import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

/**
 * Manages a set of {@link Competitor} and {@link Boat} objects. There may be a transient implementation based on a simple cache,
 * and there may be persistent implementations.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CompetitorStore extends CompetitorFactory, BoatFactory {
    public interface CompetitorUpdateListener {
        void competitorUpdated(Competitor competitor);
    }

    public interface BoatUpdateListener {
        void boatUpdated(Boat boat);
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

    int getCompetitorsCount();

    /**
     * Removes all competitors from this store. Use with due care.
     */
    void clearCompetitors();
    
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
    Competitor updateCompetitor(String idAsString, String newName, String newShortName, Color newDisplayColor, String newEmail,
            String newSailId, Nationality newNationality, URI newTeamImageUri, URI newFlagImageUri,
            Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag);

    CompetitorDTO convertToCompetitorDTO(Competitor c);
    
    /**
     * Listeners added here are notified whenever {@link #updateCompetitor(String, String, Color, String, Nationality)} is called
     * for any competitor in this store.
     */
    void addCompetitorUpdateListener(CompetitorUpdateListener listener);
    
    void removeCompetitorUpdateListener(CompetitorUpdateListener listener);
    
    /**
     * If a valid boat is returned and the caller has information available that could be used to update the boat,
     * the caller must check the result of {@link #isBoatToUpdateDuringGetOrCreate(Competitor)}, and if <code>true</code>,
     * must call {@link #getOrCreateBoat(..)} to cause an update of the boat's values.
     */
    Boat getExistingBoatByIdAsString(String idAsString);
    
    /**
     * When a boat is queried using {@link #getOrCreateBoat(...)},
     * and the boat object for that ID already exists, it is generally returned unchanged, and the properties
     * are not evaluated. This makes the data in this store generally "write-once." This
     * method can be used to reset a boat object to what a tracking provider or an external system supplies to
     * {@link #getOrCreateBoat(...)}. After calling this method, the
     * next call to {@link #getOrCreateBoat(...)} with <code>boat</code>'s {@link Boat#getId() ID} will
     * {@link #updateBoat(...)} the boat in its updatable properties such as sail ID and the color.
     */
    void allowBoatResetToDefaults(Boat boat);

    int getBoatsCount();

    /**
     * Removes all boats from this store. Use with due care.
     */
    void clearBoats();
    
    /**
     * Obtains a non-live snapshot of the list of boats managed by this store.
     */
    Iterable<? extends Boat> getBoats();
    
    void removeBoat(Boat boat);

    /**
     * Updates the boat with {@link Boat#getId() ID} <code>id</code> by setting the name, sail ID, etc. to
     * the values provided. Doing so will not fire any events nor will it replicate this change from a master to any replicas.
     * The calling client has to make sure that the changes applied will reach replicas and all other interested clients. It will
     * be sufficient to ensure that subsequent DTOs produced from the competitor modified will reflect the changes.<p>
     * 
     * If no boat with the ID requested is found, the call is a no-op, doing nothing, not even throwing an exception.
     */
    Boat updateBoat(String idAsString, String newName, Color newColor, String newSailId);

    BoatDTO convertToBoatDTO(Boat boat);
    
    /**
     * Listeners added here are notified whenever {@link #updateBoat(...)} is called
     * for any boat in this store.
     */
    void addBoatUpdateListener(BoatUpdateListener listener);
    
    void removeBoatUpdateListener(BoatUpdateListener listener);
}
