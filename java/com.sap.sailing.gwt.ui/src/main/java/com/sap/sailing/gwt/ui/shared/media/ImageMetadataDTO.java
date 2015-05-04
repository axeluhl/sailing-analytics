package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;
import java.util.Date;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.ImageSize;

public class ImageMetadataDTO extends ImageReferenceDTO {
    
    private String title;
    private String subtitle;
    private Date createdAtDate;
    private String author;
    
    // TODO do we need this?
//    private String thumbnailURL;
//    private int thumbnailWidthInPx;
//    private int thumbnailHeightInPx;
    
    protected ImageMetadataDTO() {
    }

    @GwtIncompatible
    public ImageMetadataDTO(URL imageURL, ImageSize size, String title) {
        super(imageURL, size);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
