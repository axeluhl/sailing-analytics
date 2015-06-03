package com.sap.sse.common.media;

import java.net.URL;

import com.sap.sse.common.TimePoint;


public class ImageDescriptorImpl extends AbstractMediaDescriptor implements ImageDescriptor {
    private static final long serialVersionUID = -702731462768602331L;

    private Integer widthInPx;
    private Integer heightInPx;

    /**
     * @param imageURL
     * @param size
     */
    public ImageDescriptorImpl(URL imageURL, TimePoint createdAtDate) {
        super(imageURL, MimeType.image, createdAtDate);
    }

    @Override
    public Integer getWidthInPx() {
        return widthInPx;
    }

    @Override
    public Integer getHeightInPx() {
        return heightInPx;
    }

    @Override
    public void setSize(ImageSize size) {
        if (size != null) {
            this.widthInPx = size.getWidth();
            this.heightInPx = size.getHeight();
        } else {
            this.widthInPx = null;
            this.heightInPx = null;
        }
    }

    @Override
    public void setSize(int widthInPx, int heightInPx) {
        this.widthInPx = widthInPx;
        this.heightInPx = heightInPx;
    }
}
