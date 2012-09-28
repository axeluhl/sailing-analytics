package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MediaTrack implements IsSerializable {

    public enum MimeFileType {
        
        mp4(MediaType.VIDEO, MediaSubType.mpeg), mp3(MediaType.AUDIO, MediaSubType.mpeg), ogv(MediaType.VIDEO, MediaSubType.ogg), qt(MediaType.VIDEO, MediaSubType.quicktime);
        
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
        ogg, mpeg, x_aiff, quicktime;
        
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

    public String title;
    public String url;
    public Date startTime;
    public MediaType type;
    public MediaSubType subType;

    public MediaTrack() {
        super();
    }

    public MediaTrack(String title, String url, Date startTime, MediaType type, MediaSubType subType) {
        super();
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.type = type;
        this.subType = subType;
    }

}
