package com.sap.sailing.gwt.home.client.place.event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.place.event.header.EventHeader;
import com.sap.sailing.gwt.home.client.place.event.media.EventMedia;
import com.sap.sailing.gwt.home.client.place.event.overview.EventOverview;
import com.sap.sailing.gwt.home.client.place.event.regattalist.EventRegattaList;
import com.sap.sailing.gwt.home.client.place.event.regattaraces.EventRegattaRaces;
import com.sap.sailing.gwt.home.client.place.event.schedule.EventSchedule;
import com.sap.sailing.gwt.home.client.shared.eventsponsors.EventSponsors;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewConfiguration;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Pair;
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
    @UiField(provided=true) EventRegattaRaces eventRegattaRaces;
    
    @UiField EventSponsors eventSponsors;

    private final List<Widget> pageElements;
    private final Map<String, Pair<StrippedLeaderboardDTO, LeaderboardGroupDTO>> leaderboardsWithLeaderboardGroup;
    
    public TabletAndDesktopEventView(SailingServiceAsync sailingService, EventDTO event, List<RaceGroupDTO> raceGroups, String leaderboardName,   
            Timer timerForClientServerOffset) {
        leaderboardsWithLeaderboardGroup = new HashMap<String, Pair<StrippedLeaderboardDTO, LeaderboardGroupDTO>>();
        for(LeaderboardGroupDTO leaderboardGroup: event.getLeaderboardGroups()) {
            for(StrippedLeaderboardDTO leaderboard: leaderboardGroup.getLeaderboards()) {
                leaderboardsWithLeaderboardGroup.put(leaderboard.name, new Pair<StrippedLeaderboardDTO, LeaderboardGroupDTO>(leaderboard, leaderboardGroup));
            }
        }
        
        eventHeader = new EventHeader(event, this);
        eventRegattaList = new EventRegattaList(event, raceGroups, leaderboardsWithLeaderboardGroup, timerForClientServerOffset, this);
        eventRegattaRaces = new EventRegattaRaces(event, timerForClientServerOffset, this);
        eventOverview = new EventOverview(event);
        eventSchedule = new EventSchedule(event);
        eventMedia = new EventMedia(event);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageElements = Arrays.asList(new Widget[] { eventOverview, eventRegattaList, eventRegattaRaces, eventMedia, eventSchedule });

        int leaderboardsCount = raceGroups.size();
        if(leaderboardsCount == 0) {
            Window.alert("There is no data available for this event.");
        } else {
            RaceGroupDTO selectedRaceGroup = null;

            // find the preselected leaderboard (if one exist)
            if (leaderboardName != null) {
                for (RaceGroupDTO raceGroup : raceGroups) {
                    if(raceGroup.getName().equals(leaderboardName)) {
                        selectedRaceGroup = raceGroup;
                        break;
                    }
                }
            }

            // in case we only have one regatta/leaderboard we go directly to the 'races' page
            if(selectedRaceGroup == null && leaderboardsCount == 1) {
                selectedRaceGroup = raceGroups.get(0);
            }

            if(selectedRaceGroup != null) {
                goToRegattaRaces(selectedRaceGroup, leaderboardsWithLeaderboardGroup.get(selectedRaceGroup.getName()).getA());
            } else {
                goToRegattas();
            }
            if(event.getSponsorImageURLs() != null && event.getSponsorImageURLs().size() > 0) {
                eventSponsors.setVisible(false);
                eventSponsors.setEventSponsors(event.getSponsorImageURLs());
            } else {
                eventSponsors.setVisible(false);
                eventSponsors.setEventSponsors(null);
            }
            
        }
    }

    @Override
    public void updateEventRaceStates(List<RegattaOverviewEntryDTO> racesStateEntries) {
        
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
    public void goToRegattaRaces(RaceGroupDTO raceGroup, StrippedLeaderboardDTO leaderboard) {
        eventRegattaRaces.setRacesFromRaceGroup(raceGroup, leaderboard);
        eventHeader.setDataNavigationType("compact");
        setVisibleEventElement(eventRegattaRaces);
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
        String link = EntryPointLinkFactory.createLeaderboardLink(createLeaderboardLinkParameters(leaderboardGroup, leaderboard));
        Window.open(link, "_blank", "");
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

    private Map<String, String> createLeaderboardLinkParameters(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("name", leaderboard.name);
        linkParams.put("showRaceDetails", "true");
        if (leaderboard.displayName != null) {
            linkParams.put("displayName", leaderboard.displayName);
        }
        if(leaderboardGroup != null) {
            linkParams.put("leaderboardGroupName", leaderboardGroup.getName());
        }
        return linkParams;
    }

    private void setVisibleEventElement(Widget visibleWidget) {
        for (Widget element : pageElements) {
            element.setVisible(element == visibleWidget);
        }
    }

}
