package com.sap.sailing.gwt.ui.shared.media;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sse.common.media.MimeType;

public final class MediaConstants {
    
    public static final String STAGE = "Stage";
    public static final String TEASER = "Teaser";
    public static final String HIGHLIGHT = "Highlight";
    public static final String LIVESTREAM = "Livestream";
    public static final String FEATURED = "Featured";
    public static final String LOCALE_PREFIX = "Locale_";
    public static final String LOCALE_EN = LOCALE_PREFIX + "en";
    public static final List<String> imageTagSuggestions = Arrays.asList(new String[] { STAGE, TEASER, "Sponsor" });
    public static final List<String> videoTagSuggestions = Arrays.asList(new String[] { LIVESTREAM, HIGHLIGHT, FEATURED, LOCALE_PREFIX + "de", LOCALE_EN, LOCALE_PREFIX + "ru", LOCALE_PREFIX + "cn" });
    public static final Set<MimeType> SUPPORTED_VIDEO_TYPES = new HashSet<>(Arrays.asList(MimeType.youtube, MimeType.vimeo, MimeType.mp4));
    
    private MediaConstants() {
    }

}
