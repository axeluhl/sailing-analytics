package com.sap.sailing.gwt.home.communication.media;

import java.util.Date;

import com.sap.sailing.gwt.home.communication.event.EventLinkDTO;
import com.sap.sse.gwt.client.media.AbstractMediaDTO;
import com.sap.sse.gwt.client.media.ImageDTO;

public class SailingImageDTO extends ImageDTO {
    
    private EventLinkDTO eventLink;

    protected SailingImageDTO() {
        super();
    }

    public SailingImageDTO(EventLinkDTO eventLink, String imageRef, Date createdAtDate) {
        super(imageRef, createdAtDate);
        this.eventLink = eventLink;
    }

    public EventLinkDTO getEventLink() {
        return eventLink;
    }

    // TODO Move to {@link AbstractMediaDTO} ---
    @Override
    public int compareTo(AbstractMediaDTO o) {
        int createdAtDateComp = compareToByCreatedAtDate(o); 
        return createdAtDateComp == 0 ? getSourceRef().compareTo(o.getSourceRef()) : createdAtDateComp;
    }
    
    private int compareToByCreatedAtDate(AbstractMediaDTO o) {
        if(getCreatedAtDate() == o.getCreatedAtDate()) {
            return 0;
        }
        if(getCreatedAtDate() == null) {
            return 1;
        }
        if(o.getCreatedAtDate() == null) {
            return -1;
        }
        return -getCreatedAtDate().compareTo(o.getCreatedAtDate());
    }
    // TODO END ---
}
