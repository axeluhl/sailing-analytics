package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.WithID;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.common.Named;

public interface Competitor extends Named, WithID, IsManagedByCache {
    @Connector(messageKey="Team", ordinal=9)
    Team getTeam();

    @Connector(messageKey="Boat", ordinal=10)
    Boat getBoat();
    
    Color getColor();
    
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
}
