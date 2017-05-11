package com.sap.sailing.gwt.autoplay.client.configs;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public abstract class AutoPlayConfiguration {

    public abstract void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings);

    public void loadSettings(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard, Holder settingsHolder) {
        Window.alert("This configuration does not have settings");
    }

    public static class Holder {
        private PerspectiveCompositeSettings<?> settings;

        public PerspectiveCompositeSettings<?> getSettings() {
            return settings;
        }

        public void setSettings(PerspectiveCompositeSettings<?> settings) {
            this.settings = settings;
        }

        public boolean isNull() {
            return settings == null;
        }
    }
}
