package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoplayPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoplayPerspectiveOwnSettings;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class AutoPlayContextImpl implements AutoPlayContext {
    private AutoPlaySettings settings;
    private EventBus eventBus;
    private EventDTO event;
    private RegattaAndRaceIdentifier lifeRace;
    private RegattaAndRaceIdentifier lastRace;
    private AutoplayPerspectiveLifecycle autoplayLifecycle;
    private PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings;


    public AutoPlayContextImpl(EventBus eventBus, AutoplayPerspectiveLifecycle autoplayLifecycle,
            PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplaySettings, AutoPlaySettings settings) {
       if (autoplayLifecycle == null) {
            throw new IllegalStateException("No autoplayLifecycle in creation");
        }
        if (autoplaySettings == null) {
            throw new IllegalStateException("No autoplaySettings in creation");
        }
        if (settings == null) {
            throw new IllegalStateException("No settings in creation");
        }
        this.eventBus = eventBus;
        this.settings = settings;
        this.autoplayLifecycle = autoplayLifecycle;
        this.autoplaySettings = autoplaySettings;
    }

    public AutoPlayContextImpl(EventBus eventBus, AutoPlaySettings settings) {
        this.eventBus = eventBus;
        this.settings = settings;
    }

    @Override
    public AutoPlaySettings getSettings() {
        return settings;
    }

    @Override
    public RegattaAndRaceIdentifier getLifeRace() {
        return lifeRace;
    }

    @Override
    public void setCurrenLifeRace(RegattaAndRaceIdentifier lifeRace) {
        if(this.lifeRace != null){
            if(!this.lifeRace.equals(lifeRace)){
                this.lastRace = this.lifeRace;
                GWT.log("lastrace is not " + lastRace);
            }
        }
        this.lifeRace = lifeRace;
    }

    @Override
    public EventDTO getEvent() {
        return event;
    }

    @Override
    public void updateEvent(EventDTO event) {
        this.event = event;
        eventBus.fireEvent(new EventChanged(event));
    }

    @Override
    public RegattaAndRaceIdentifier getLastRace() {
        return lastRace;
    }

    public PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> getAutoplaySettings() {
        return autoplaySettings;
    }

    public AutoplayPerspectiveLifecycle getAutoplayLifecycle() {
        return autoplayLifecycle;
    }
}
