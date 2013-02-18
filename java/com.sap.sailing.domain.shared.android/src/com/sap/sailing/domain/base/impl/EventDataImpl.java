package com.sap.sailing.domain.base.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.domain.base.Venue;

public class EventDataImpl implements EventData {
	private static final long serialVersionUID = -5749964088848611074L;
	
	private String name;
    private String publicationUrl;
    private final Venue venue;
    private boolean isPublic;
    private final Serializable id;

    public EventDataImpl(String name, String venueName, String publicationUrl, boolean isPublic, Serializable id) {
        this(name, new VenueImpl(venueName), publicationUrl, isPublic, id);
    }
    
    /**
     * @param venue must not be <code>null</code>
     */
    public EventDataImpl(String name, Venue venue, String publicationUrl, boolean isPublic, Serializable id) {
        assert venue != null;
        this.id = id;
        this.name = name;
        this.venue = venue;
        this.publicationUrl = publicationUrl;
        this.isPublic = isPublic;
    }

	@Override
    public Serializable getId() {
        return id;
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
