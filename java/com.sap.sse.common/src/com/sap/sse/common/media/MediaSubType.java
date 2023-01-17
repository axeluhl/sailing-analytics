package com.sap.sse.common.media;

public enum MediaSubType {
    ogg, ogv, mp4, mpeg, x_aiff, quicktime, aac, webm, youtube, vimeo, unknown;

    public String toString() {
        return name().replace('_', '-');
    };

}