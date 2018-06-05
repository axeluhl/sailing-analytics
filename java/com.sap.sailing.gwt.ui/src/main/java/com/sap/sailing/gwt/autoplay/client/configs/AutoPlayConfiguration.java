package com.sap.sailing.gwt.autoplay.client.configs;

import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;

public abstract class AutoPlayConfiguration {
    public abstract void startRootNode(AutoPlayClientFactory cf, AutoPlayContextDefinition context,
            PerspectiveCompositeSettings<?> settings, EventDTO initialEventData);

    /**
     * This method should be pure/standalone, it is not allowed to make any guesses on fields written by startRootNode,
     * as it can be called before that
     */
    public void openSettingsDialog(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard,
            OnSettingsCallback holder, PerspectiveCompositeSettings<?> settings, AutoPlayContextDefinition apcd,
            UserService userService) {
        Window.alert("This configuration does not have settings");

    }

    /**
     * This method should be pure/standalone, it is not allowed to make any guesses on fields written by startRootNode,
     * as it can be called before that
     */
    public void loadSettingsDefault(EventDTO selectedEvent, StrippedLeaderboardDTO leaderboard, UserService userService,
            OnSettingsCallback holder) {
        holder.newSettings(null);
    }

    public interface OnSettingsCallback {
        void newSettings(PerspectiveCompositeSettings<?> newSettings);
    }

}
