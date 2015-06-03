package com.sap.sse.common.media;

import java.net.URL;
import java.util.Date;


public class ImageMetadataImpl extends AbstractMediaMetadata {
    private static final long serialVersionUID = -702731462768602331L;

    private int widthInPx;
    private int heightInPx;

    /**
     * @param imageURL
     * @param size
     */
    public ImageMetadataImpl(URL imageURL, ImageSize size, Date createdAtDate) {
        super(imageURL, MimeType.image, createdAtDate);
        this.widthInPx = size.getWidth();
        this.heightInPx = size.getHeight();
    }

    public ImageMetadataImpl(URL imageURL, int heightInPx, int widthInPx, Date createdAtDate) {
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
