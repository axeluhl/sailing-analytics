package com.sap.sailing.gwt.ui.client.media;

import java.io.Serializable;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MediaPlayerLifecycle implements ComponentLifecycle<MediaPlayerSettings, MediaPlayerSettingsDialogComponent> {
    
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
    public MediaPlayerSettings cloneSettings(MediaPlayerSettings settings) {
        return new MediaPlayerSettings(settings.isAutoSelectMedia());
    }
    
    @Override
    public String getLocalizedShortName() {
        return stringMessages.videoComponentShortName();
    }

    @Override
    public Serializable getComponentId() {
        return getLocalizedShortName();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}
