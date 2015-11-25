package com.sap.sse.shared.media;

import com.sap.sse.common.Util.Pair;

public interface ImageDescriptor extends MediaDescriptor {
    void setSize(Pair<Integer, Integer> size);
    void setSize(Integer widthInPx, Integer heightInPx);

    Integer getWidthInPx();
    Integer getHeightInPx();
    
    boolean hasSize();
    int getArea();
}
