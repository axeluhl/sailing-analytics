package com.sap.sailing.domain.common.media;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.Duration;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;

/**
 * See http://my.opera.com/core/blog/2010/03/03/everything-you-need-to-know-about-html5-video-and-audio-2
 * 
 * @author D047974
 * 
 */
public class MediaTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum MimeType {

        mp4(MediaType.video, MediaSubType.mp4), ogv(MediaType.video, MediaSubType.ogg), qt(MediaType.video,
                MediaSubType.quicktime), mp3(MediaType.audio, MediaSubType.mpeg), ogg(MediaType.audio, MediaSubType.ogg), aac(
                MediaType.audio, MediaSubType.aac), webm(MediaType.video, MediaSubType.webm), youtube(MediaType.video,
                MediaSubType.youtube);

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
    public TimePoint startTime;
    public Duration duration;
    public MimeType mimeType;
    public Status status = Status.UNDEFINED;
    public Set<RegattaAndRaceIdentifier> regattasAndRaces;

    public MediaTrack() {
        regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
    }

    public MediaTrack(String title, String url, TimePoint startTime, Duration duration, MimeType mimeType,
            Set<RegattaAndRaceIdentifier> regattasAndRaces) {
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.duration = duration;
        this.mimeType = mimeType;
        if (regattasAndRaces != null) {
            this.regattasAndRaces = regattasAndRaces;
        }else{
            regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
        }
    }

    public MediaTrack(String dbId, String title, String url, TimePoint startTime, Duration duration, MimeType mimeType,
            Set<RegattaAndRaceIdentifier> regattasAndRaces) {
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.duration = duration;
        this.mimeType = mimeType;
        if (regattasAndRaces != null) {
            this.regattasAndRaces = regattasAndRaces;
        }
    }

    public String toString() {
        String regattasAndRaces = "";
        for (RegattaAndRaceIdentifier regattaAndRace : this.regattasAndRaces) {
            regattasAndRaces += ", " + regattaAndRace;
        }
        return title + " - " + url + " [" + typeToString() + ']' + " - " + regattasAndRaces + " - " + startTime + " [" + duration + status + ']'; 
    }

    public TimePoint deriveEndTime() {
        if (startTime != null) {
            return startTime.plus(duration);
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

    /**
     * Checks for overlap of this start time and duration with the given startTime and endTime, excluding boundaries!
     * Behaviour for given endTime being earlier than given startTime is not specified. endTime being null represents
     * "open end". Open beginning is not allow, though!
     * 
     * @param startTime
     *            Must not be null.
     * @param endTime
     *            May be null representing "open end".
     */
    public boolean overlapsWith(TimePoint startTime, TimePoint endTime) {
        if (this.startTime == null) {
            return false;
        } else {
            return this.deriveEndTime().after(startTime) && (endTime == null || this.startTime.before(endTime));
        }
    }

    public boolean isConnectedTo(RegattaAndRaceIdentifier race) {
        if (regattasAndRaces.contains(race)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaTrack) {
            MediaTrack mediaTrack = (MediaTrack) obj;
            return this.dbId == null ? mediaTrack.dbId == null : this.dbId.equals(mediaTrack.dbId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.dbId == null ? 0 : this.dbId.hashCode();
    }

    public boolean beginsAfter(Date date) {
        if (startTime.asDate().after(date)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean endsBefore(Date date) {
        if (deriveEndTime().asDate().before(date)) {
            return true;
        } else {
            return false;
        }
    }

}
