package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class SlideContextImpl implements SlideContext {
    private SixtyInchSetting settings;

    private GetMiniLeaderboardDTO miniLeaderboardDTO;
    private EventBus eventBus;

    private EventDTO event;

    public SlideContextImpl(EventBus eventBus, SixtyInchSetting settings) {
        if (settings == null) {
            throw new IllegalStateException("No settings in ctx creation");
        }
        this.eventBus = eventBus;
        this.settings = settings;
    }

    @Override
    public SixtyInchSetting getSettings() {
        return settings;
    }

    @Override
    public void updateMiniLeaderboardDTO(GetMiniLeaderboardDTO miniLeaderboardDTO) {
        this.miniLeaderboardDTO = miniLeaderboardDTO;
        eventBus.fireEvent(new MiniLeaderboardUpdatedEvent());
    }

    @Override
    public GetMiniLeaderboardDTO getMiniLeaderboardDTO() {
        return miniLeaderboardDTO;
    }

    @Override
    public void updateEvent(EventDTO event) {
        if (detectChange(this.event, event)) {
            this.event = event;
            eventBus.fireEvent(new EventChanged(event));
        }
    }

    private boolean detectChange(Object event2, EventDTO event3) {
        return true;
    }

    @Override
    public EventDTO getEvent() {
        return event;
    }
}
