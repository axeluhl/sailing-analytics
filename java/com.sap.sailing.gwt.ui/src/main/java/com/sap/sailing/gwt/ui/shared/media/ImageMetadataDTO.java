package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;
import java.util.Date;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class ImageMetadataDTO extends AbstractMediaDTO {
    
    private static final long serialVersionUID = 1L;

    private int widthInPx;
    private int heightInPx;

    private String subtitle;
    private Date createdAtDate;
    private String author;

    protected ImageMetadataDTO() {
    }

    @GwtIncompatible
    public ImageMetadataDTO(EventReferenceDTO eventRef, URL imageURL, ImageSize size, String title) {
        super(eventRef, imageURL.toString(), MimeType.image, title);
        this.widthInPx = size.getWidth();
        this.heightInPx = size.getHeight();
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

    public int getWidthInPx() {
        return widthInPx;
    }

    public void setWidthInPx(int widthInPx) {
        this.widthInPx = widthInPx;
    }

    public int getHeightInPx() {
        return heightInPx;
    }

    public void setHeightInPx(int heightInPx) {
        this.heightInPx = heightInPx;
    }

}
