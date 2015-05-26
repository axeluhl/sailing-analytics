package com.sap.sse.common.media;

import java.util.Date;

public class ImageMetadataDTO extends AbstractMediaDTO {
    
    private static final long serialVersionUID = 1L;

    private int widthInPx;
    private int heightInPx;

    private String subtitle;
    private Date createdAtDate;
    private String author;


    protected ImageMetadataDTO() {
    }

    public ImageMetadataDTO(String imageURL, ImageSize size, String title) {
        super(imageURL, MimeType.image, title);
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
