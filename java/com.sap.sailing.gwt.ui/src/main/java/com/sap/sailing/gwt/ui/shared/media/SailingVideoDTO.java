package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.client.media.AbstractMediaDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class SailingVideoDTO extends VideoDTO {
    
    private EventReferenceDTO eventRef;

    /** for GWT */
    protected SailingVideoDTO() {
    }

    public SailingVideoDTO(EventReferenceDTO eventRef, String srcReference, MimeType mimeType, Date createdAtDate) {
        super(srcReference, mimeType, createdAtDate);
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
