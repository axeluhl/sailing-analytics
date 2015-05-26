package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.media.ImageMetadataDTO;
import com.sap.sse.common.media.ImageSize;

public class SailingImageDTO extends ImageMetadataDTO {
    
    private static final long serialVersionUID = 1L;

    private String subtitle;
    private Date createdAtDate;
    private String author;

    private EventReferenceDTO eventRef;

    protected SailingImageDTO() {
    }


    public SailingImageDTO(EventReferenceDTO eventRef, String imageURL, ImageSize size, String title) {
        super(imageURL, size, title);
        this.eventRef = eventRef;

    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Date getCreatedAtDate() {
        return createdAtDate;
    }

    public void setCreatedAtDate(Date createdAtDate) {
        this.createdAtDate = createdAtDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }



    public EventReferenceDTO getEventRef() {
        return eventRef;
    }
}
