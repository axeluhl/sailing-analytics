package com.sap.sailing.gwt.ui.raceboard;

import java.util.UUID;

import com.sap.sse.gwt.client.shared.perspective.ComponentContext;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class RaceBoardContext extends ComponentContext<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> {
    
    public RaceBoardContext(String entryPointId, RaceBoardPerspectiveLifecycle raceBoardPerspectiveLifecycle, String regattaName, String raceName, String leaderboardName,
            String leaderboardGroupName, UUID eventId) {
        super(entryPointId, raceBoardPerspectiveLifecycle, regattaName, raceName, leaderboardName, leaderboardGroupName, eventId == null ? null : eventId.toString());
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractContextSpecificSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
        return newRootPerspectiveSettings;
    }

    @Override
    protected PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> extractGlobalSettings(
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
        return newRootPerspectiveSettings;
    }
    
}
