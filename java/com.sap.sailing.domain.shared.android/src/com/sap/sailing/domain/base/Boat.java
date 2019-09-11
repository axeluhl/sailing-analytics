package com.sap.sailing.domain.base;

import com.sap.sse.common.Color;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithID;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * The boat being sailed by a competitor.
 *
 */
public interface Boat extends NamedWithID, IsManagedByCache<SharedDomainFactory>, WithQualifiedObjectIdentifier {
    BoatClass getBoatClass();   
    
    @Dimension(messageKey="SailID")
    String getSailID();
    
    Color getColor();
    
    /**
     * Adds a listener to this boat.
     * Adding a listener that is already part of this boat's listeners set remains without effect.
     */
    void addBoatChangeListener(BoatChangeListener listener);
    
    /**
     * Removes a listener from this boat.
     * Trying to remove a listener that is not currently in the set of this boats's listener remains
     * without effect.
     */
    void removeBoatChangeListener(BoatChangeListener listener);

}
