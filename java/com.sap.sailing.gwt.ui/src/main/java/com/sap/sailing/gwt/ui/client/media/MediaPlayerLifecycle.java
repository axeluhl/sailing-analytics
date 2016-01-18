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

public class MediaPlayerLifecycle implements ComponentLifecycle<MediaPlayerManagerComponent, MediaPlayerSettings, MediaPlayerSettingsDialogComponent,   
    MediaPlayerLifecycle.MediaPlayerManagerConstructorArgs> {
    
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
        return new MediaPlayerSettings();
    }

    @Override
    public MediaPlayerSettings cloneSettings(MediaPlayerSettings settings) {
        return new MediaPlayerSettings(settings.isAutoSelectMedia());
    }

    @Override
    public MediaPlayerManagerComponent createComponent(MediaPlayerManagerConstructorArgs contructorArgs, MediaPlayerSettings settings) {
        return contructorArgs.createComponent(settings);
    }
    
    @Override
    public String getLocalizedShortName() {
        return stringMessages.videoComponentShortName();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    public class MediaPlayerManagerConstructorArgs implements ComponentConstructorArgs<MediaPlayerManagerComponent, MediaPlayerSettings> {
        private final RegattaAndRaceIdentifier selectedRaceIdentifier;
        private final RaceTimesInfoProvider raceTimesInfoProvider;
        private final Timer raceTimer;
        private final MediaServiceAsync mediaService;
        private final UserService userService;
        private final StringMessages stringMessages;
        private final ErrorReporter errorReporter;
        private final UserAgentDetails userAgent;
        private final PopupPositionProvider popupPositionProvider;
        private final MediaPlayerSettings settings;
        
        public MediaPlayerManagerConstructorArgs(RegattaAndRaceIdentifier selectedRaceIdentifier,
                RaceTimesInfoProvider raceTimesInfoProvider, Timer raceTimer, MediaServiceAsync mediaService,
                UserService userService, StringMessages stringMessages, ErrorReporter errorReporter,
                UserAgentDetails userAgent, PopupPositionProvider popupPositionProvider, MediaPlayerSettings settings) {
            this.selectedRaceIdentifier = selectedRaceIdentifier;
            this.raceTimesInfoProvider = raceTimesInfoProvider;
            this.raceTimer = raceTimer;
            this.mediaService = mediaService;
            this.userService = userService;
            this.stringMessages = stringMessages;
            this.errorReporter = errorReporter;
            this.userAgent = userAgent;
            this.popupPositionProvider = popupPositionProvider;
            this.settings = settings;
        }
        
        @Override
        public MediaPlayerManagerComponent createComponent(MediaPlayerSettings newSettings) {
            MediaPlayerManagerComponent mediaPlayerComponent = new MediaPlayerManagerComponent(selectedRaceIdentifier,
                    raceTimesInfoProvider, raceTimer, mediaService,
                    userService, stringMessages, errorReporter,
                    userAgent, popupPositionProvider, settings);
            if (newSettings != null) {
                mediaPlayerComponent.updateSettings(newSettings);
            }
            return mediaPlayerComponent;
        }
    }
}
