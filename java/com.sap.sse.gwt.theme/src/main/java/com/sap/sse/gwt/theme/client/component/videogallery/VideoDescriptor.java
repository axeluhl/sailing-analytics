package com.sap.sse.gwt.theme.client.component.videogallery;

import java.util.Date;

public class VideoDescriptor {
    private String title;
    private String subtitle;
    private Date createdAtDate;
    private String author;

    private int widthInPx;
    private int heightInPx;
    private String videoURL;

    private String thumbnailURL;
    private int thumbnailWidthInPx;
    private int thumbnailHeightInPx;
    
    public VideoDescriptor(String videoURL) {
        this.videoURL = videoURL;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    
    public String getThumbnailURL() {
        return thumbnailURL;
    }
    
    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
    
    public String getVideoURL() {
        return videoURL;
    }
    
    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
