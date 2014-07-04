package com.sap.sailing.gwt.home.client.place.event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.place.event.header.EventHeader;
import com.sap.sailing.gwt.home.client.place.event.media.EventMedia;
import com.sap.sailing.gwt.home.client.place.event.overview.EventOverview;
import com.sap.sailing.gwt.home.client.place.event.regattalist.EventRegattaList;
import com.sap.sailing.gwt.home.client.place.event.regattaschedule.EventRegattaSchedule;
import com.sap.sailing.gwt.home.client.place.event.schedule.EventSchedule;
import com.sap.sailing.gwt.home.client.shared.eventsponsors.EventSponsors;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewConfiguration;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class TabletAndDesktopEventView extends Composite implements EventView, EventPageNavigator {
    private static EventViewUiBinder uiBinder = GWT.create(EventViewUiBinder.class);

    interface EventViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField(provided=true) EventHeader eventHeader;
    @UiField(provided=true) EventOverview eventOverview;
    @UiField(provided=true) EventSchedule eventSchedule;
    @UiField(provided=true) EventMedia eventMedia;
    @UiField(provided=true) EventRegattaList eventRegattaList;
    @UiField(provided=true) EventRegattaSchedule eventRegattaSchedule;
    
    @UiField EventSponsors eventSponsors;

    private final List<Widget> pageElements;
    
    private final SailingServiceAsync sailingService;
    
    public TabletAndDesktopEventView(SailingServiceAsync sailingService, EventDTO event, Timer timerForClientServerOffset) {
        this.sailingService = sailingService;
        eventHeader = new EventHeader(event, this);
        eventRegattaList = new EventRegattaList(event, this);
        eventRegattaSchedule = new EventRegattaSchedule(event, timerForClientServerOffset, this);
        eventOverview = new EventOverview(event);
        eventSchedule = new EventSchedule(event);
        eventMedia = new EventMedia(event);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageElements = Arrays.asList(new Widget[] { eventOverview, eventRegattaList, eventRegattaSchedule, eventMedia, eventSchedule });
        goToRegattas();

        eventSponsors.setEventSponsors(event);
    }

    @Override
    public void goToOverview() {
        setVisibleEventElement(eventOverview);
        eventHeader.setDataNavigationType("normal");
    }

    @Override
    public void goToRegattas() {
        setVisibleEventElement(eventRegattaList);
        eventHeader.setDataNavigationType("normal");
    }

    @Override
    public void goToRegattaRaces(final LeaderboardGroupDTO leaderboardGroup, final StrippedLeaderboardDTO leaderboard) {
        switch (leaderboard.type) {
        case RegattaLeaderboard:
            sailingService.getRegattaByName(leaderboard.regattaName, new AsyncCallback<RegattaDTO>() {
                @Override
                public void onSuccess(RegattaDTO regatta) {
                    eventRegattaSchedule.setRacesFromRegatta(regatta, leaderboardGroup, leaderboard);
                    eventHeader.setDataNavigationType("compact");
                    setVisibleEventElement(eventRegattaSchedule);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Shit happens");
                }
            });
            break;
        case FlexibleLeaderboard:
            eventRegattaSchedule.setRacesFromFlexibleLeaderboard(leaderboard);
            eventHeader.setDataNavigationType("compact");
            setVisibleEventElement(eventRegattaSchedule);
            break;
        case FlexibleMetaLeaderboard:
            eventHeader.setDataNavigationType("compact");
            setVisibleEventElement(eventRegattaSchedule);
            break;
        case RegattaMetaLeaderboard:
            eventHeader.setDataNavigationType("compact");
            setVisibleEventElement(eventRegattaSchedule);
            break;
        }
    }

    @Override
    public void goToSchedule() {
        setVisibleEventElement(eventSchedule);
        eventHeader.setDataNavigationType("normal");
    }

    @Override
    public void goToMedia() {
        setVisibleEventElement(eventMedia);
        eventHeader.setDataNavigationType("normal");
    }

    @Override
    public void openRaceViewer(StrippedLeaderboardDTO leaderboard, RaceDTO race) {
        RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier();
        String link = EntryPointLinkFactory.createRaceBoardLink(createRaceBoardLinkParameters(leaderboard.name, raceIdentifier));
        Window.open(link, "_blank", "");
    }
    
    @Override
    public void openLeaderboardViewer(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        
    }

    private Map<String, String> createRaceBoardLinkParameters(String leaderboardName, RegattaAndRaceIdentifier raceIdentifier) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("leaderboardName", leaderboardName);
        linkParams.put("raceName", raceIdentifier.getRaceName());
        linkParams.put(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, "true");
        linkParams.put(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, "true");
        linkParams.put(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_NAVIGATION_PANEL, "true");
        linkParams.put("regattaName", raceIdentifier.getRegattaName());
        return linkParams;
    }

    private void setVisibleEventElement(Widget visibleWidget) {
        for (Widget element : pageElements) {
            element.setVisible(element == visibleWidget);
        }
    }

}
