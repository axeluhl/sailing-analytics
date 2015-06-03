package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.media.ImageSize;
import com.sap.sse.gwt.client.media.ImageDTO;

public class SailingImageDTO extends ImageDTO {
    
    private EventReferenceDTO eventRef;

    protected SailingImageDTO() {
        super();
    }

    public SailingImageDTO(EventReferenceDTO eventRef, String imageRef, ImageSize size, Date createdAtDate) {
        super(imageRef, size, createdAtDate);
        this.eventRef = eventRef;

    }

    public EventReferenceDTO getEventRef() {
        return eventRef;
    }
}
