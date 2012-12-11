package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MediaTrack implements IsSerializable {

    public enum MimeFileType {
        
        mp4(MediaType.VIDEO, MediaSubType.mpeg), ogv(MediaType.VIDEO, MediaSubType.ogg), qt(MediaType.VIDEO, MediaSubType.quicktime), mp3(MediaType.AUDIO, MediaSubType.mpeg), ogg(MediaType.AUDIO, MediaSubType.ogg), aac(MediaType.AUDIO, MediaSubType.aac), webm(MediaType.VIDEO, MediaSubType.webm);
        
        public final MediaType mediaType;
        public final MediaSubType mediaSubType;
        
        MimeFileType(MediaType mediaType, MediaSubType mediaSubType) {
            this.mediaType = mediaType;
            this.mediaSubType = mediaSubType;            
        }
        
        @Override
        public String toString() {
            return mediaType.name() + '/' + mediaSubType.toString();
        }
    }
    
    public enum MediaType {
        AUDIO, VIDEO;
    }

    public enum MediaSubType {
        ogg, mpeg, x_aiff, quicktime, aac, webm;
        
        public String toString() {
            return name().replace('_', '-');
        };
        
    }

    public class MediaSection implements IsSerializable {
        public String title;
        public long offsetInMillis;
        public Date startTime;
        public Date endTime;
    }

    public String dbId;
    public String title;
    public String url;
    public Date startTime;
    public int durationInMillis;
    public MediaType type;
    public MediaSubType subType;

    public MediaTrack() {
        super();
    }

    public MediaTrack(String title, String url, Date startTime, int durationInMillis, MediaType mediaType, MediaSubType mediaSubType) {
        this(null, title, url, startTime, durationInMillis, mediaType, mediaSubType);
    }

    public MediaTrack(String dbId, String title, String url, Date startTime, int durationInMillis, MediaType mediaType,
            MediaSubType mediaSubType) {
        super();
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.durationInMillis = durationInMillis;
        this.type = mediaType;
        this.subType = mediaSubType;
    }
    
    public String toString() {
        return title + " - " + url + " [" + type + '/' + subType + ']' + startTime + " [" + durationInMillis / 1000d + "s]";  
    }

}
