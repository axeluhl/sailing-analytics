package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * See http://my.opera.com/core/blog/2010/03/03/everything-you-need-to-know-about-html5-video-and-audio-2
 * @author D047974
 *
 */
public class MediaTrack implements IsSerializable {

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
    public MimeType mimeType;

    public MediaTrack() {
        super();
    }

    public MediaTrack(String dbId, String title, String url, Date startTime, int durationInMillis, MimeType mimeType) {
        super();
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.durationInMillis = durationInMillis;
        this.mimeType = mimeType;
    }
    
    public String toString() {
        return title + " - " + url + " [" + typeToString() + ']' + startTime + " [" + durationInMillis + ']';  
    }

    public String typeToString() {
        return mimeType == null ? "undefined" : mimeType.toString();
    }
    
    public boolean isYoutube() {
        return (mimeType != null) && MediaSubType.youtube.equals(mimeType.mediaSubType);
    }

}
