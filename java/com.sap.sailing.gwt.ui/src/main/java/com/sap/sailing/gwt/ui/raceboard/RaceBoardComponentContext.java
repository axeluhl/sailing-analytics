package com.sap.sailing.gwt.ui.raceboard;

import java.util.UUID;

import com.sap.sse.gwt.client.shared.perspective.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class RaceBoardComponentContext
        extends ComponentContextWithSettingsStorage<RaceBoardPerspectiveLifecycle, PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>> {

    public RaceBoardComponentContext(UserService userService, String entryPointId, RaceBoardPerspectiveLifecycle raceBoardPerspectiveLifecycle,
            String regattaName, String raceName, String leaderboardName, String leaderboardGroupName, UUID eventId) {
        super(raceBoardPerspectiveLifecycle, new UserSettingsStorageManager<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>>(userService, entryPointId + "." + raceBoardPerspectiveLifecycle.getComponentId(), UserSettingsStorageManager.buildContextDefinitionId(regattaName, raceName, leaderboardName, leaderboardGroupName,
                eventId == null ? null : eventId.toString())));
    }


}
