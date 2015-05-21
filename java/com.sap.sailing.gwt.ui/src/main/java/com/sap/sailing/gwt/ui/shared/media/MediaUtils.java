package com.sap.sailing.gwt.ui.shared.media;

import java.util.regex.Pattern;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.media.MimeType;

/**
 * Temporary solution for mimetype detection for current videos.
 * 
 * @author pgtaboada
 *
 */
public class MediaUtils {

    /**
     * Youtube regex detection from:
     * http://stackoverflow.com/questions/3452546/javascript-regex-how-to-get-youtube-video-id-from-url, mantish Mar 4
     * at 15:33
     */
    @GwtIncompatible
    private static final Pattern YOUTUBE_ID_REGEX = Pattern
            .compile("^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|\\&v=)([^#\\&\\?]+).*$");

    @GwtIncompatible
    private static final Pattern VIMEO_REGEX = Pattern.compile("^.*(vimeo\\.com\\/).*");

    @GwtIncompatible
    private static final Pattern MP4_REGEX = Pattern.compile(".*\\.mp4$");

    /**
     * Detect mimetype for given url.
     * 
     * @param url
     *            the source pointing to the video mediafile
     * @return mimetype detected or MimeType.unknown
     */
    @GwtIncompatible
    public static MimeType detectMimeTypeFromUrl(String url) {

        if (YOUTUBE_ID_REGEX.matcher(url).matches()) {
            return MimeType.youtube;
        } else if (VIMEO_REGEX.matcher(url).matches()) {
            return MimeType.vimeo;
        } else if (MP4_REGEX.matcher(url).matches()) {
            return MimeType.mp4;
        } else {
            return MimeType.unknown;
        }
    }

}
