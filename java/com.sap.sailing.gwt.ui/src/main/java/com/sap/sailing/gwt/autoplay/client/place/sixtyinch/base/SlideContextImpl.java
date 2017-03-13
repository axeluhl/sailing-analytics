package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import java.util.Date;
import java.util.Map;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.events.RaceTimeInfoProviderUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class SlideContextImpl implements SlideContext {
    private SixtyInchSetting settings;

    private GetMiniLeaderboardDTO miniLeaderboardDTO;
    private EventBus eventBus;

    private EventDTO event;

    private Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo;

    private long clientTimeWhenRequestWasSent;

    private Date serverTimeDuringRequest;

    private long clientTimeWhenResponseWasReceived;

    private RegattaAndRaceIdentifier lifeRace;

    private RaceTimesInfoProvider raceTimesInfoProvider;

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
    public void updateMiniLeaderboardDTO(GetMiniLeaderboardDTO miniLeaderboardDTO) {
        this.miniLeaderboardDTO = miniLeaderboardDTO;
        eventBus.fireEvent(new MiniLeaderboardUpdatedEvent());
    }

    @Override
    public void updateEvent(EventDTO event) {
        if (detectChange(this.event, event)) {
            this.event = event;
            eventBus.fireEvent(new EventChanged(event));
        }
    }

    @Override
    public GetMiniLeaderboardDTO getMiniLeaderboardDTO() {
        return miniLeaderboardDTO;
    }

    private boolean detectChange(Object event2, EventDTO event3) {
        return true;
    }

    @Override
    public EventDTO getEvent() {
        return event;
    }

    public Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> getRaceTimesInfo() {
        return raceTimesInfo;
    }

    @Override
    public void updateRaceTimeInfos(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived,
            RegattaAndRaceIdentifier lifeRace) {
        this.raceTimesInfo = raceTimesInfo;
        this.clientTimeWhenRequestWasSent = clientTimeWhenRequestWasSent;
        this.serverTimeDuringRequest = serverTimeDuringRequest;
        this.clientTimeWhenResponseWasReceived = clientTimeWhenResponseWasReceived;
        this.lifeRace = lifeRace;
        eventBus.fireEvent(new RaceTimeInfoProviderUpdatedEvent());
    }

    @Override
    public long getClientTimeWhenRequestWasSent() {
        return clientTimeWhenRequestWasSent;
    }

    @Override
    public Date getServerTimeDuringRequest() {
        return serverTimeDuringRequest;
    }

    @Override
    public long getClientTimeWhenResponseWasReceived() {
        return clientTimeWhenResponseWasReceived;
    }

    @Override
    public RegattaAndRaceIdentifier getCurrentLiveRace() {
        return lifeRace;
    }

    @Override
    public RaceTimesInfoProvider getRaceTimesInfoProvider() {
        return raceTimesInfoProvider;
    }

    @Override
    public void setRaceTimesInfoProvider(RaceTimesInfoProvider raceTimesInfoProvider) {
        this.raceTimesInfoProvider = raceTimesInfoProvider;
    }
}
