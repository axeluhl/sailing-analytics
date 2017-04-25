package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public interface AutoPlayContext {
    AutoPlaySettings getSettings();

    void updateEvent(EventDTO event);

    EventDTO getEvent();

    public RegattaAndRaceIdentifier getLifeRace();

    public RegattaAndRaceIdentifier getLastRace();

    public void setCurrenLifeRace(RegattaAndRaceIdentifier lifeRace);

    PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> getAutoplaySettings();

    AutoplayPerspectiveLifecycle getAutoplayLifecycle();
}
