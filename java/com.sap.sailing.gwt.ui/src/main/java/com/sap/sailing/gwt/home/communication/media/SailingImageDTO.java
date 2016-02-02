package com.sap.sailing.gwt.home.communication.media;

import java.util.Date;

import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sse.gwt.client.media.AbstractMediaDTO;
import com.sap.sse.gwt.client.media.ImageDTO;

public class SailingImageDTO extends ImageDTO {
    
    private EventReferenceDTO eventRef;

    protected SailingImageDTO() {
        super();
    }

    public SailingImageDTO(EventReferenceDTO eventRef, String imageRef, Date createdAtDate) {
        super(imageRef, createdAtDate);
        this.eventRef = eventRef;
    }

    public EventReferenceDTO getEventRef() {
        return eventRef;
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
