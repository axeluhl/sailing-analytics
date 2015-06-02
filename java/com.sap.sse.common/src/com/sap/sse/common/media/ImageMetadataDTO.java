package com.sap.sse.common.media;

import java.util.Date;


public class ImageMetadataDTO extends AbstractMediaDTO {
    
    private static final long serialVersionUID = 1L;

    private int widthInPx;
    private int heightInPx;

    protected ImageMetadataDTO() {
    }

    /**
     * TODO: should we use imagesize, or should we store height and width? For now, this is a convenience method...
     * 
     * @param imageURL
     * @param size
     */
    public ImageMetadataDTO(String imageURL, ImageSize size, Date createdAtDate) {
        super(imageURL, MimeType.image, createdAtDate);
        this.widthInPx = size.getWidth();
        this.heightInPx = size.getHeight();
    }

    public ImageMetadataDTO(String imageURL, int heightInPx, int widthInPx, Date createdAtDate) {
        super(imageURL, MimeType.image, createdAtDate);
        this.widthInPx = widthInPx;
        this.heightInPx = heightInPx;
    }

    public int getWidthInPx() {
        return widthInPx;
    }

    public int getHeightInPx() {
        return heightInPx;
    }

}
