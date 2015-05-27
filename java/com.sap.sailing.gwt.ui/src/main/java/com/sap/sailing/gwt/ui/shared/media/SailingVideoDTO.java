package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoMetadataDTO;

public class SailingVideoDTO extends VideoMetadataDTO {
    
    private static final long serialVersionUID = 1L;

    private EventReferenceDTO eventRef;

    @SuppressWarnings("unused")
    private SailingVideoDTO() {
    }

    public SailingVideoDTO(EventReferenceDTO eventRef, String srcReference, MimeType mimeType, Date createdAtDate) {
        super(srcReference, mimeType, createdAtDate);
        this.eventRef = eventRef;
    }
    

    public EventReferenceDTO getEventRef() {
        return eventRef;
    }

}
