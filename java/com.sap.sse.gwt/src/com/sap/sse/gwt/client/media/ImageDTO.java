package com.sap.sse.gwt.client.media;

import java.util.Date;

import com.sap.sse.common.media.ImageSize;
import com.sap.sse.common.media.MimeType;

public class ImageDTO extends AbstractMediaDTO {

    /** for GWT */
    protected ImageDTO() {
    }
    
    /** can be null if not known */
    private Integer widthInPx;
    private Integer heightInPx;

    public ImageDTO(String imageRef, ImageSize size, Date createdAtDate) {
        super(imageRef, MimeType.image, createdAtDate);
        this.widthInPx = size.getWidth();
        this.heightInPx = size.getHeight();
    }

    public ImageDTO(String imageRef, Integer widthInPx, Integer heightInPx, Date createdAtDate) {
        super(imageRef, MimeType.image, createdAtDate);
        this.widthInPx = widthInPx;
        this.heightInPx = heightInPx;
    }

    public Integer getWidthInPx() {
        return widthInPx;
    }

    public Integer getHeightInPx() {
        return heightInPx;
    }
}
