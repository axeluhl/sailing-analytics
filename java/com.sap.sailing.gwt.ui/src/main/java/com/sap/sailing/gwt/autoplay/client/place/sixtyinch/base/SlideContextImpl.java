package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class SlideContextImpl implements SlideContext {
    private SixtyInchSetting settings;

    private EventBus eventBus;

    private EventDTO event;

    public SlideContextImpl(EventBus eventBus, SixtyInchSetting settings) {
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
    public SixtyInchSetting getSettings() {
        return settings;
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
}
