package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.WithID;

/**
 * Base interface for an Event consisting of all static information, which might be shared
 * by the server and an Android application.
 */
public interface EventData extends Named, Renamable, WithID {

    /**
     * @return a non-<code>null</code> venue for this event
     */
    Venue getVenue();

    String getPublicationUrl();

    void setPublicationUrl(String publicationUrl);

    boolean isPublic();

    void setPublic(boolean isPublic);
}
