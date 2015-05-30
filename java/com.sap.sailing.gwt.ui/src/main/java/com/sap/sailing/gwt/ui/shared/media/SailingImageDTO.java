package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.media.ImageMetadataDTO;
import com.sap.sse.common.media.ImageSize;

public class SailingImageDTO extends ImageMetadataDTO {
    
    private static final long serialVersionUID = 1L;

    private EventReferenceDTO eventRef;

    @SuppressWarnings("unused")
    private SailingImageDTO() {
    }

    public SailingImageDTO(EventReferenceDTO eventRef, String imageURL, ImageSize size, Date createdAtDate) {
        super(imageURL, size, createdAtDate);
        this.eventRef = eventRef;

    }

    public EventReferenceDTO getEventRef() {
        return eventRef;
    }
}
