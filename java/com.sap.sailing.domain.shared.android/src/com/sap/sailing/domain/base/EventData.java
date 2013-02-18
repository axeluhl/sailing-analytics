package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.common.WithID;

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
