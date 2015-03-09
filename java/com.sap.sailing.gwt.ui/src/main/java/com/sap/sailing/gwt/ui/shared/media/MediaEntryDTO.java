package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

public class MediaEntryDTO {
    private String title;
    private String subtitle;
    private Date createdAtDate;
    private String author;

    private int widthInPx;
    private int heightInPx;
    private String mediaURL;

    private String thumbnailURL;
    private int thumbnailWidthInPx;
    private int thumbnailHeightInPx;

    public MediaEntryDTO() {
    }

    public MediaEntryDTO(String title, String mediaURL) {
        this.title = title;
        this.mediaURL = mediaURL;
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

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public int getThumbnailWidthInPx() {
        return thumbnailWidthInPx;
    }

    public void setThumbnailWidthInPx(int thumbnailWidthInPx) {
        this.thumbnailWidthInPx = thumbnailWidthInPx;
    }

    public int getThumbnailHeightInPx() {
        return thumbnailHeightInPx;
    }

    public void setThumbnailHeightInPx(int thumbnailHeightInPx) {
        this.thumbnailHeightInPx = thumbnailHeightInPx;
    }

}
