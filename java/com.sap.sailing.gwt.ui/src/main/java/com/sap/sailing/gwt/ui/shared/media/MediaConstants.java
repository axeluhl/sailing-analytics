package com.sap.sailing.gwt.ui.shared.media;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;

public final class MediaConstants {
    
    public static final List<String> imageTagSuggestions = Arrays.asList(new String[] { MediaTagConstants.STAGE, MediaTagConstants.TEASER, MediaTagConstants.SPONSOR, MediaTagConstants.LOGO });
    public static final List<String> videoTagSuggestions = Arrays.asList(new String[] { MediaTagConstants.LIVESTREAM, MediaTagConstants.HIGHLIGHT,MediaTagConstants.FEATURED });
    public static final Set<MimeType> SUPPORTED_VIDEO_TYPES = new HashSet<>(Arrays.asList(MimeType.youtube, MimeType.vimeo, MimeType.mp4));
    
    private MediaConstants() {
    }

}
