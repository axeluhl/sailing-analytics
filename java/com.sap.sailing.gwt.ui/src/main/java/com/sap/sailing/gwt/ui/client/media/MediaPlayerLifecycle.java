package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MediaPlayerLifecycle implements ComponentLifecycle<MediaPlayerSettings> {
    
    private final StringMessages stringMessages;
    
    public MediaPlayerLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public MediaPlayerSettingsDialogComponent getSettingsDialogComponent(MediaPlayerSettings settings) {
        return new MediaPlayerSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public MediaPlayerSettings createDefaultSettings() {
        return MediaPlayerSettings.readSettingsFromURL();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.videoComponentShortName();
    }

    @Override
    public String getComponentId() {
        return "mpl";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MediaPlayerSettings extractGlobalSettings(MediaPlayerSettings settings) {
        return createDefaultSettings();
    }

    @Override
    public MediaPlayerSettings extractContextSettings(MediaPlayerSettings settings) {
        return createDefaultSettings();
    }
}
