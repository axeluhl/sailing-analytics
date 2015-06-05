package com.sap.sse.common.media;

public interface ImageDescriptor extends MediaDescriptor {
    void setSize(ImageSize size);
    void setSize(Integer widthInPx, Integer heightInPx);

    Integer getWidthInPx();
    Integer getHeightInPx();
}
