package com.sap.sailing.gwt.autoplay.client.configs;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public abstract class AutoPlayConfiguration {

    public abstract void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings);

    public void openSettingsDialog(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard,
            OnSettingsCallback holder, PerspectiveCompositeSettings<?> settings,AutoPlayContextDefinition apcd) {
        Window.alert("This configuration does not have settings");

    }

    public void loadSettingsDefault(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard,
            OnSettingsCallback holder) {
        holder.newSettings(null);
    }

    public interface OnSettingsCallback {
        void newSettings(PerspectiveCompositeSettings<?> newSettings);
    }
}
