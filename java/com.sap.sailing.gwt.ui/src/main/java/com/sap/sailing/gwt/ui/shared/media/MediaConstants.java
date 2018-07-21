package com.sap.sailing.gwt.ui.shared.media;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;

public final class MediaConstants {
    
    // perfect fit would be 1440 x 580
    public static final int MIN_STAGE_IMAGE_WIDTH = 1440 / 2;
    public static final int MAX_STAGE_IMAGE_WIDTH = 1440 * 2;
    public static final int MIN_STAGE_IMAGE_HEIGHT = 580 / 2;
    public static final int MAX_STAGE_IMAGE_HEIGHT = 580 * 2;

    // perfect fit would be 370 x 240
    public static final int MIN_EVENTTEASER_IMAGE_WIDTH = 370 / 2;
    public static final int MAX_EVENTTEASER_IMAGE_WIDTH = 370 * 3;
    public static final int MIN_EVENTTEASER_IMAGE_HEIGHT = 240 / 2;
    public static final int MAX_EVENTTEASER_IMAGE_HEIGHT = 240 * 3;
    
    // perfect fit would be 140 x 140
    public static final int MIN_LOGO_IMAGE_WIDTH = 140 / 2;
    public static final int MAX_LOGO_IMAGE_WIDTH = 140 * 4;
    public static final int MIN_LOGO_IMAGE_HEIGHT = 140 / 2;
    public static final int MAX_LOGO_IMAGE_HEIGHT = 140 * 4;

    public static final List<String> imageTagSuggestions = Arrays.asList(new String[] { MediaTagConstants.BIGSCREEN,MediaTagConstants.STAGE, MediaTagConstants.TEASER, 
            MediaTagConstants.SPONSOR, MediaTagConstants.LOGO, MediaTagConstants.GALLERY });
    public static final List<String> videoTagSuggestions = Arrays.asList(new String[] { MediaTagConstants.BIGSCREEN,MediaTagConstants.LIVESTREAM, MediaTagConstants.HIGHLIGHT, MediaTagConstants.FEATURED, MediaTagConstants.STAGE });
    public static final Set<MimeType> SUPPORTED_VIDEO_TYPES = new HashSet<>(Arrays.asList(MimeType.youtube, MimeType.vimeo, MimeType.mp4, MimeType.mp4panorama, MimeType.mp4panoramaflip));
    
    private MediaConstants() {
    }

}
