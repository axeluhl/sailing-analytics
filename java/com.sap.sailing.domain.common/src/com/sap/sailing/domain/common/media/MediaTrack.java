package com.sap.sailing.domain.common.media;

import java.io.Serializable;
import java.util.Date;

/**
 * See http://my.opera.com/core/blog/2010/03/03/everything-you-need-to-know-about-html5-video-and-audio-2
 * @author D047974
 *
 */
public class MediaTrack implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public enum MimeType {
        
        mp4(MediaType.video, MediaSubType.mp4), ogv(MediaType.video, MediaSubType.ogg), qt(MediaType.video, MediaSubType.quicktime), mp3(MediaType.audio, MediaSubType.mpeg), ogg(MediaType.audio, MediaSubType.ogg), aac(MediaType.audio, MediaSubType.aac), webm(MediaType.video, MediaSubType.webm), youtube(MediaType.video, MediaSubType.youtube);
        
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
    }
    
    public enum MediaType {
        audio, video;
    }

    public enum MediaSubType {
        ogg, mp4, mpeg, x_aiff, quicktime, aac, webm, youtube;
        
        public String toString() {
            return name().replace('_', '-');
        };
        
    }
    
    public enum Status {
        UNDEFINED('?'), CANNOT_PLAY('-'), NOT_REACHABLE('#'), REACHABLE('+');
        
        private final char symbol;
        
        private Status(char symbol) {
            this.symbol = symbol;
        }
        
        public String toString() {
            return String.valueOf(this.symbol); 
        }
    }

    public String dbId;
    public String title;
    public String url;
    public Date startTime;
    public int durationInMillis;
    public MimeType mimeType;
    public Status status = Status.UNDEFINED;

    public MediaTrack() {
    }
    
    public MediaTrack(String dbId, String title, String url, Date startTime, int durationInMillis, MimeType mimeType) {
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.durationInMillis = durationInMillis;
        this.mimeType = mimeType;
    }
    
    public String toString() {
        return title + " - " + url + " [" + typeToString() + ']' + startTime + " [" + durationInMillis + status + ']';  
    }
    
    public Date deriveEndTime() {
        if (startTime != null) {
            return new Date(startTime.getTime() + durationInMillis);
        } else {
            return null;
        }
    }

    public String typeToString() {
        return mimeType == null ? "undefined" : mimeType.toString();
    }
    
    public boolean isYoutube() {
        return (mimeType != null) && MediaSubType.youtube.equals(mimeType.mediaSubType);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaTrack) {
            MediaTrack mediaTrack = (MediaTrack) obj;
            return this.dbId.equals(mediaTrack.dbId);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return this.dbId == null ? 0 : this.dbId.hashCode();
    }

}
