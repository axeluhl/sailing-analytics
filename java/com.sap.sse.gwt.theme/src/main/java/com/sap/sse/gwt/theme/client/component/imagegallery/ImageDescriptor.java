package com.sap.sse.gwt.theme.client.component.imagegallery;

import java.util.Date;

public class ImageDescriptor {
    private String title;
    private String subtitle;
    private Date createdAtDate;
    private String author;

    private int widthInPx;
    private int heightInPx;
    private String imageURL;

    private String thumbnailURL;
    private int thumbnailWidthInPx;
    private int thumbnailHeightInPx;
    
    public ImageDescriptor(String imageURL) {
        this.imageURL = imageURL;
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
    
    public String getImageURL() {
        return imageURL;
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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
