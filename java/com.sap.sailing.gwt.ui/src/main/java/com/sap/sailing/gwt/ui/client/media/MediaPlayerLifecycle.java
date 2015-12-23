package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.client.UserService;

public class MediaPlayerLifecycle implements ComponentLifecycle<MediaPlayerManagerComponent, /*MediaPlayerLifecycle.MediaPlayerManagerConstructorArgs, */MediaPlayerSettings, MediaPlayerSettingsDialogComponent> {
    private final StringMessages stringMessages;
    
    private MediaPlayerManagerComponent component;
    
    public MediaPlayerLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.component = null;
    }

    @Override
    public MediaPlayerSettingsDialogComponent getSettingsDialogComponent(MediaPlayerSettings settings) {
        return new MediaPlayerSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public MediaPlayerSettings createDefaultSettings() {
        return new MediaPlayerSettings();
    }

    @Override
    public MediaPlayerSettings cloneSettings(MediaPlayerSettings settings) {
        return new MediaPlayerSettings(settings.isAutoSelectMedia());
    }

    public MediaPlayerManagerComponent createComponent(MediaPlayerManagerConstructorArgs contructorArgs) {
        this.component = contructorArgs.getCreatedComponent();
        return this.component;
    }
    
    public static class MediaPlayerManagerConstructorArgs implements ComponentConstructorArgs<MediaPlayerManagerComponent, MediaPlayerSettings> {
        private final MediaPlayerManagerComponent component; 

        public MediaPlayerManagerConstructorArgs(RegattaAndRaceIdentifier selectedRaceIdentifier,
                RaceTimesInfoProvider raceTimesInfoProvider, Timer raceTimer, MediaServiceAsync mediaService,
                UserService userService, StringMessages stringMessages, ErrorReporter errorReporter,
                UserAgentDetails userAgent, PopupPositionProvider popupPositionProvider, MediaPlayerSettings settings) {
            component = new MediaPlayerManagerComponent(selectedRaceIdentifier,
                    raceTimesInfoProvider, raceTimer, mediaService,
                    userService, stringMessages, errorReporter,
                    userAgent, popupPositionProvider, settings);
        }
        public MediaPlayerManagerComponent getCreatedComponent() {
            return component;
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.videoComponentShortName();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MediaPlayerManagerComponent getComponent() {
        return component;
    }
}
