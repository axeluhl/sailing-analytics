package com.sap.sse.common.media;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public enum MimeType {

    mp4(MediaType.video, MediaSubType.mp4, "mp4"),
    ogv(MediaType.video, MediaSubType.ogg, "ogv"),
    qt(MediaType.video, MediaSubType.quicktime, "qt|qtvr|qti|qtif"),
    mp3(MediaType.audio, MediaSubType.mpeg, "mp3"),
    ogg(MediaType.audio, MediaSubType.ogg, "ogg|oga|spx"),
    aac(MediaType.audio, MediaSubType.aac, "acc"),
    webm(MediaType.video, MediaSubType.webm, "webm"),
    youtube(MediaType.video,  MediaSubType.youtube, ""),
    vimeo(MediaType.video, MediaSubType.vimeo, ""),
    image(MediaType.image, MediaSubType.unknown, "jpg|jpeg|jpe|jif|jfif|jfi|png|gif|webp|tiff|tif|apng|avif|svg|bmp|ico|cur"),
    unknown(MediaType.unknown, MediaSubType.unknown, ""),
    mp4panorama(MediaType.video, MediaSubType.mp4, "mp4"),
    mp4panoramaflip(MediaType.video, MediaSubType.mp4, "mp4"),
    mov(MediaType.video, MediaSubType.mp4, "mov|quicktime");

    public final MediaType mediaType;
    public final MediaSubType mediaSubType;
    public final String endingPattern;

    MimeType(MediaType mediaType, MediaSubType mediaSubType, String endingPattern) {
        this.mediaType = mediaType;
        this.mediaSubType = mediaSubType;
        this.endingPattern = endingPattern;
    }

    public boolean isPanorama() {
        return this == mp4panorama || this == mp4panoramaflip;
    }

    public boolean isFlippedPanorama() {
        return this == mp4panoramaflip;
    }

    public String getEndingPattern() {
        return endingPattern;
    }

    public MediaSubType getMediaSubType() {
        return mediaSubType;
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

    public static MimeType[] mp4MimeTypes() {
        return new MimeType[] { mp4, mp4panorama, mp4panoramaflip, mov };
    }

    /**
     * Parses a file name by it's extension and return the matching {@link MimeType}.
     * 
     * @param fileName
     *            String of the file name
     * @return the matching MimeTpye or else {@link MimeType#unknown}
     */
    public static MimeType byExtension(String fileName) {
        final MimeType result;
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos >= 0) {
            String fileEnding = fileName.substring(dotPos + 1).toLowerCase();
            MimeType bestMatch = unknown;
            if (fileEnding != null) {
                for (MimeType mimeType : MimeType.values()) {
                    if (!mimeType.getEndingPattern().isEmpty()) {
                        RegExp regExp = RegExp.compile(mimeType.getEndingPattern());
                        MatchResult matcher = regExp.exec(fileEnding.toLowerCase());
                        boolean matchFound = matcher != null;
                        if (matchFound) {
                            bestMatch = mimeType;
                            break;
                        }
                    }
                }
            }
            result = bestMatch;
        } else {
            result = unknown;
        }
        return result;
    }

    /**
     * Gets a mime type based on content type, e.g. image/jpeg, video/mp4, ...
     * 
     * The media type must always match (image, video, audio). Then it will compare the sub type first by mime type
     * name, then by ending pattern and then by sub type name
     * 
     * @param contentType
     *            the content type of the media in String form
     * @return the matching {@link MimeType} or else {@link MimeType#unknown}
     */
    public static MimeType byContentType(String contentType) {
        final MimeType result;
        if (contentType != null && contentType.contains("/")) {
            String mediaType = contentType.split("/")[0];
            String subType = contentType.split("/")[1];
            MimeType bestMatch = unknown;
            for (MimeType mimeType : MimeType.values()) {
                if (mimeType.mediaType.name().equals(mediaType) 
                        && (mimeType.name().equals(subType)
                                || mimeType.getEndingPattern().contains(subType.toLowerCase())
                                || mimeType.getMediaSubType().name().equals(subType))) {
                    bestMatch = mimeType;
                    break;
                }
            }
            result = bestMatch;
        } else {
            result = unknown;
        }
        return result;
    }

}