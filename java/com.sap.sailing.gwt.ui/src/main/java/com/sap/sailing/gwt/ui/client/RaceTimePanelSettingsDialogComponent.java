package com.sap.sailing.gwt.ui.client;

import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;

public class RaceTimePanelSettingsDialogComponent extends TimePanelSettingsDialogComponent<RaceTimePanelSettings> {
    
    public RaceTimePanelSettingsDialogComponent(RaceTimePanelSettings settings, StringMessages stringMessages,
            UserService userService, final SecuredDTO raceDTO) {
        super(settings, stringMessages, userService, raceDTO);
    }

    @Override
    public RaceTimePanelSettings getResult() {
        long refreshInternal = refreshIntervalBox.getValue() == null ? -1 : (long) (refreshIntervalBox.getValue() * 1000);
        RaceTimePanelSettings result = new RaceTimePanelSettings(refreshInternal);
        return result;
    }
}
