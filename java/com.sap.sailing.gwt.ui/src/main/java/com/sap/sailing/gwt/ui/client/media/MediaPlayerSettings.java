package com.sap.sailing.gwt.ui.client.media;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class MediaPlayerSettings extends AbstractSettings {
    private final boolean autoPlayMedia;

    public static final String PARAM_AUTOPLAY_MEDIA = "autoSelectMedia";

    /**
     *  The default settings
     */
    public MediaPlayerSettings() {
        this.autoPlayMedia = true;
    }
    
    public MediaPlayerSettings(boolean autoPlayMedia) {
        this.autoPlayMedia = autoPlayMedia;
    }

    public boolean isAutoSelectMedia() {
        return autoPlayMedia;
    }
    
    public static MediaPlayerSettings readSettingsFromURL() {
        final Boolean autoPlayMedia = GwtHttpRequestUtils.getBooleanParameter(PARAM_AUTOPLAY_MEDIA, true /* default */);

        return new MediaPlayerSettings(autoPlayMedia);
    }

}
