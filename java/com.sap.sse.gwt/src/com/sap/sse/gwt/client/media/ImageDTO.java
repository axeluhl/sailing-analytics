package com.sap.sse.gwt.client.media;

import java.util.Date;

import com.sap.sse.common.media.MimeType;

public class ImageDTO extends AbstractMediaDTO {

    /** for GWT */
    @Deprecated
    protected ImageDTO() {
    }
    
    /** can be null if not known */
    private Integer widthInPx;
    private Integer heightInPx;

    public ImageDTO(String imageRef, Date createdAtDate) {
        super(imageRef, MimeType.image, createdAtDate);
    }

    public Integer getWidthInPx() {
        return widthInPx;
    }

    public Integer getHeightInPx() {
        return heightInPx;
    }

    public void setSizeInPx(Integer widthInPx, Integer heightInPx) {
        this.widthInPx = widthInPx;
        this.heightInPx = heightInPx;
    }
}
