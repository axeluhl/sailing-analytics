package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Venue;

public class EventImpl implements Event {
    private static final long serialVersionUID = 855135446595485715L;
    
    private final Set<Regatta> regattas;
    private String name;
    private String publicationUrl;
    private final Venue venue;
    private boolean isPublic;
    private final Serializable id;

    public EventImpl(String name, String venueName, String publicationUrl, boolean isPublic, Serializable id) {
        this(name, new VenueImpl(venueName), publicationUrl, isPublic, id);
    }
    
    /**
     * @param venue must not be <code>null</code>
     */
    public EventImpl(String name, Venue venue, String publicationUrl, boolean isPublic, Serializable id) {
        assert venue != null;
        this.id = id;
        this.name = name;
        this.venue = venue;
        this.publicationUrl = publicationUrl;
        this.isPublic = isPublic;
        this.regattas = new HashSet<Regatta>();
    }

	@Override
    public Serializable getId() {
        return id;
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

    @Override
    public Venue getVenue() {
        return venue;
    }

    @Override
    public String getPublicationUrl() {
        return publicationUrl;
    }

    @Override
    public void setPublicationUrl(String publicationUrl) {
        this.publicationUrl = publicationUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param newName must not be <code>null</code>
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("An event name must not be null");
        }
        this.name = newName;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
