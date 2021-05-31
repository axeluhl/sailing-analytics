package com.sap.sse.common.media;

import java.util.ArrayList;
import java.util.List;

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
    
    public boolean isPanorama(){
        return this == mp4panorama || this == mp4panoramaflip;
    }
    
    public boolean isFlippedPanorama(){
        return this == mp4panoramaflip;
    }
    
    private String getEndingPattern() {
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
        return new MimeType[] { mp4, mp4panorama, mp4panoramaflip, mov};
    }
    
    public static List<MimeType> byExtension(String extension) {
        List<MimeType> result = new ArrayList<>();
        if (extension != null) {
            for (MimeType mimeType: MimeType.values()) {
                if (mimeType.getEndingPattern().contains(extension.toLowerCase())) {
                    result.add(mimeType);
                }
            }
        }
        return result;
    }
    
    public static List<MimeType> fromUrl(String url) {
        List<MimeType> result = new ArrayList<>();
        if (url != null) {
            for (MimeType mimeType: MimeType.values()) {
                if (mimeType.endingPattern.length() > 0) {
                    String regex = "[a-z\\-_0-9\\/\\:\\.]*\\.(" + mimeType.getEndingPattern() + ")";
                    if (RegExp.compile(regex, "i").test(url)) {
                        result.add(mimeType);
                    }
                }
            }
        }
        return result;
    }

}