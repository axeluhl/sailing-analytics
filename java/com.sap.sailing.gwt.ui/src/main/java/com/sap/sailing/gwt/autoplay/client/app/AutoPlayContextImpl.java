package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class AutoPlayContextImpl implements AutoPlayContext {
    private AutoPlaySettings settings;
    private EventBus eventBus;
    private EventDTO event;
    private RegattaAndRaceIdentifier lifeRace;
    private RegattaAndRaceIdentifier lastRace;


    public AutoPlayContextImpl(EventBus eventBus, AutoPlaySettings settings) {
        if (settings == null) {
            throw new IllegalStateException("No settings in ctx creation");
        }
        if (eventBus == null) {
            throw new IllegalStateException("No settings in eventBus creation");
        }
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
}
