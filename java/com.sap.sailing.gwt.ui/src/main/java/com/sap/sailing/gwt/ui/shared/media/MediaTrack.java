package com.sap.sailing.gwt.ui.shared.media;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.adminconsole.TimeFormatUtil;

/**
 * See http://my.opera.com/core/blog/2010/03/03/everything-you-need-to-know-about-html5-video-and-audio-2
 * @author D047974
 *
 */
public class MediaTrack implements IsSerializable {

    public enum MimeFileType {
        
        mp4(MediaType.video, MediaSubType.mpeg), ogv(MediaType.video, MediaSubType.ogg), qt(MediaType.video, MediaSubType.quicktime), mp3(MediaType.audio, MediaSubType.mpeg), ogg(MediaType.audio, MediaSubType.ogg), aac(MediaType.audio, MediaSubType.aac), webm(MediaType.video, MediaSubType.webm);
        
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
        audio, video;
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
        return title + " - " + url + " [" + typeToString() + ']' + startTimeToString() + " [" + TimeFormatUtil.milliSecondsToHrsMinSec(durationInMillis) + ']';  
    }

    public String typeToString() {
        String typeString = type == null ? "undefined" : type.name();
        String subtypeString = subType == null ? "undefined" : subType.toString();
        return typeString  + '/' + subtypeString ;
    }

    public String startTimeToString() {
        return startTime == null ? "undefined" : TimeFormatUtil.DATETIME_FORMAT.format(startTime);
    }

}
