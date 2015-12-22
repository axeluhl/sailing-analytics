package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerSettings;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ProxyMediaPlayerComponent implements Component<MediaPlayerSettings> {
    private final StringMessages stringMessages;
    private MediaPlayerSettings settings;
    
    public ProxyMediaPlayerComponent(MediaPlayerSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<MediaPlayerSettings> getSettingsDialogComponent() {
        return new MediaPlayerSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(MediaPlayerSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.mediaPanel();
    }

    @Override
    public Widget getEntryWidget() {
        throw new UnsupportedOperationException(
                "Internal error. This settings dialog does not actually belong to a wind chart");
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "MediaPlayerSettingsDialog";
    }

    @Override
    public MediaPlayerSettings getSettings() {
        return settings;
    }
}
