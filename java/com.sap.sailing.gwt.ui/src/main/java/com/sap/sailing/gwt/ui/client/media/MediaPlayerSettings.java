package com.sap.sailing.gwt.ui.client.media;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class MediaPlayerSettings extends AbstractSettings {
    private final boolean autoPlayMedia;

    public static final String PARAM_AUTOPLAY_MEDIA = "autoSelectMedia";

    public MediaPlayerSettings() {
        this.autoPlayMedia = !DeviceDetector.isMobile();
    }
    
    public MediaPlayerSettings(boolean autoPlayMedia) {
        this.autoPlayMedia = autoPlayMedia;
    }

    public boolean isAutoSelectMedia() {
        return autoPlayMedia;
    }
    
    public static MediaPlayerSettings readSettingsFromURL() {
        final Boolean autoPlayMedia = GwtHttpRequestUtils.getBooleanParameter(PARAM_AUTOPLAY_MEDIA, !DeviceDetector.isMobile());

        return new MediaPlayerSettings(autoPlayMedia);
    }

}
