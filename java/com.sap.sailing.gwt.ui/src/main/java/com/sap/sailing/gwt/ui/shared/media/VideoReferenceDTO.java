package com.sap.sailing.gwt.ui.shared.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;

public class VideoReferenceDTO implements IsSerializable {
    
    @GwtIncompatible
    private static final Pattern YOUTUBE_ID_REGEX = Pattern.compile("^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=)([^#\\&\\?]*).*");
    @GwtIncompatible
    private static final Pattern HTTP_FTP_REGEX = Pattern.compile("^(http|ftp).*"); // starting with http, https or ftp
    
    /**
     * Extracts the youtube id of the passed youtube video URL.
     * Also accepts 
     * From http://stackoverflow.com/questions/3452546/javascript-regex-how-to-get-youtube-video-id-from-url, mantish Mar 4 at 15:33
     * @param url
     * @return The youtube 
     */
    @GwtIncompatible
    private static String getIdByUrl(String url) {
        Matcher match = YOUTUBE_ID_REGEX.matcher(url);
        if ((match != null) && (match.groupCount() == 3)) {
            return match.group(2);
        } else if (HTTP_FTP_REGEX.matcher(url).matches()) { //--> doesn't start with either http, https or ftp --> supposed to be a naked youtube id   
            return url.trim();
        } else {
            return null; // --> plain http, https or ftp URL --> no youtube 
        }
    }

    public enum VideoType {
        YOUTUBE
    }

    private VideoType type;
    private String ref;

    protected VideoReferenceDTO() {
    }

    public VideoReferenceDTO(String youtubeIdOrURL) {
        type = VideoType.YOUTUBE;
        ref = getIdByUrl(youtubeIdOrURL);
    }

    public VideoType getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ref == null) ? 0 : ref.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VideoReferenceDTO other = (VideoReferenceDTO) obj;
        if (ref == null) {
            if (other.ref != null)
                return false;
        } else if (!ref.equals(other.ref))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
}
