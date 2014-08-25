package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.ImageSize;

public class ImageSizeImpl implements ImageSize {
    private static final long serialVersionUID = 1170701774852068780L;
    private int width;
    private int height;
    ImageSizeImpl() {} // for GWT serialization
    public ImageSizeImpl(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }
    @Override
    public int getWidth() {
        return width;
    }
    @Override
    public int getHeight() {
        return height;
    }
    @Override
    public String toString() {
        return "("+getWidth()+"x"+getHeight()+")";
    }
}

