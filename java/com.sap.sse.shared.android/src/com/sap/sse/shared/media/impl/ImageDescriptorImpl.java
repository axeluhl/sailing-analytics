package com.sap.sse.shared.media.impl;

import java.net.URL;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.shared.media.ImageDescriptor;


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
    public void setSize(Pair<Integer, Integer> size) {
        if (size != null) {
            this.widthInPx = size.getA();
            this.heightInPx = size.getB();
        } else {
            this.widthInPx = null;
            this.heightInPx = null;
        }
    }

    @Override
    public void setSize(Integer widthInPx, Integer heightInPx) {
        this.widthInPx = widthInPx;
        this.heightInPx = heightInPx;
    }
    
    @Override
    public boolean hasSize() {
        return widthInPx != null && heightInPx != null;
    }
    
    @Override
    public int getArea() {
        if(hasSize()) {
            return widthInPx * heightInPx;
        }
        return 0;
    }
}
