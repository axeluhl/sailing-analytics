package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinition;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public interface AutoPlayContext {
    AutoPlayConfiguration getAutoPlayConfiguration();

    AutoPlayContextDefinition getContextDefinition();

    void updateEvent(EventDTO event);

    EventDTO getEvent();

    public RegattaAndRaceIdentifier getLifeRace();

    public RegattaAndRaceIdentifier getLastRace();

    public void setCurrentLifeRace(RegattaAndRaceIdentifier lifeRace);

    PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> getAutoplaySettings();

    AutoplayPerspectiveLifecycle getAutoplayLifecycle();
}
