package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.ImageSize;

public class ImageSizeImpl implements ImageSize {
    private static final long serialVersionUID = 1170701774852068780L;
    private final int width;
    private final int height;

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
        return "(" + getWidth() + "x" + getHeight() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImageSizeImpl other = (ImageSizeImpl) obj;
        if (height != other.height)
            return false;
        if (width != other.width)
            return false;
        return true;
    }
}
