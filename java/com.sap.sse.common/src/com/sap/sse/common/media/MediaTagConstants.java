package com.sap.sse.common.media;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum MediaTagConstants {
    GALLERY("Gallery", 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE), 
    STAGE("Stage", 1440 / 2, 1440 * 2, 580 / 2, 580 * 2), // perfect fit would be 1440 x 580
    LOGO("Logo", 140 / 2, 140 * 4, 140 / 2, 140 * 4), // perfect fit would be 140 x 140
    TEASER("Teaser", 370 / 2, 370 * 3, 240 / 2, 240 * 3), // perfect fit would be 370 x 240
    SPONSOR("Sponsor", 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE), 
    HIGHLIGHT("Highlight", 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE), 
    LIVESTREAM("Livestream", 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE), 
    FEATURED("Featured", 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE), 
    BIGSCREEN("BigScreen", 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE),
    TAGGING_IMAGE("Tagging Image", 100, 100, 500, 500);

    private final String name;
    private final int minWidth;
    private final int maxWidth;
    private final int minHeight;
    private final int maxHeight;

    public static final Iterable<String> imageTagSuggestions = Arrays.asList(new String[] { BIGSCREEN.getName(),
            STAGE.getName(), TEASER.getName(), SPONSOR.getName(), LOGO.getName(), GALLERY.getName() });
    public static final Iterable<String> videoTagSuggestions = Arrays.asList(new String[] { BIGSCREEN.getName(),
            LIVESTREAM.getName(), HIGHLIGHT.getName(), FEATURED.getName(), STAGE.getName() });
    public static final Set<MimeType> SUPPORTED_VIDEO_TYPES = new HashSet<>(Arrays.asList(MimeType.youtube,
            MimeType.vimeo, MimeType.mp4, MimeType.mp4panorama, MimeType.mp4panoramaflip));

    private MediaTagConstants(String name, int minWidth, int maxWidth, int minHeight, int maxHeight) {
        this.name = name;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public String getName() {
        return name;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public static MediaTagConstants fromName(String name) {
        MediaTagConstants toReturn = null;
        for (MediaTagConstants mediaTagConstant : MediaTagConstants.values()) {
            if (mediaTagConstant.getName().equals(name)) {
                toReturn = mediaTagConstant;
            }
        }
        return toReturn;
    }

}
