package com.sap.sse.common.media;

public interface ImageDescriptor extends MediaDescriptor {
    void setSize(ImageSize size);
    void setSize(int widthInPx, int heightInPx);

    Integer getWidthInPx();
    Integer getHeightInPx();
}
