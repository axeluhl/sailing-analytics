package com.sap.sailing.domain.base.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Venue;

public class EventImpl extends EventBaseImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;
    
    private final Set<Regatta> regattas;

    public EventImpl(String name, String venueName, String publicationUrl, boolean isPublic, UUID id) {
        this(name, new VenueImpl(venueName), publicationUrl, isPublic, id);
    }
    
    /**
     * @param venue must not be <code>null</code>
     */
    public EventImpl(String name, Venue venue, String publicationUrl, boolean isPublic, UUID id) {
        super(name, venue, publicationUrl, isPublic, id);
        this.regattas = new HashSet<Regatta>();
    }

    @Override
    public Iterable<Regatta> getRegattas() {
        return Collections.unmodifiableSet(regattas);
    }

    @Override
    public void addRegatta(Regatta regatta) {
        regattas.add(regatta);
    }

    @Override
    public void removeRegatta(Regatta regatta) {
        regattas.remove(regatta);
    }
    
    public String toString() {
        return getId() + " " + getName() + " " + getVenue().getName() + " " + isPublic();
    }
}
