package com.sap.sailing.domain.common.media;

public enum MimeType {

    mp4(MediaType.video, MediaSubType.mp4), ogv(MediaType.video, MediaSubType.ogg), qt(MediaType.video,
            MediaSubType.quicktime), mp3(MediaType.audio, MediaSubType.mpeg), ogg(MediaType.audio, MediaSubType.ogg), aac(
            MediaType.audio, MediaSubType.aac), webm(MediaType.video, MediaSubType.webm), youtube(MediaType.video,
            MediaSubType.youtube), vimeo(MediaType.video, MediaSubType.vimeo), image(MediaType.image,
            MediaSubType.unknown), unknown(MediaType.unknown, MediaSubType.unknown);

    public final MediaType mediaType;
    public final MediaSubType mediaSubType;

    MimeType(MediaType mediaType, MediaSubType mediaSubType) {
        this.mediaType = mediaType;
        this.mediaSubType = mediaSubType;
    }

    @Override
    public String toString() {
        return mediaType.name() + '/' + mediaSubType.toString();
    }

    public static MimeType byName(String mimeTypeName) {
        try {
            if (mimeTypeName != null) {
                return MimeType.valueOf(mimeTypeName);
            } else {
                return null;
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}