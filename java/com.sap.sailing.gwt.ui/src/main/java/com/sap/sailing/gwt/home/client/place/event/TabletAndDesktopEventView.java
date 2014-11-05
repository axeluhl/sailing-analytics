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
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.EventNavigationTabs;
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
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.player.Timer;

public class TabletAndDesktopEventView extends Composite implements EventView, EventPlaceNavigator {
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
    private final EventDTO event;
    
    @SuppressWarnings("unused")
    private EventPlace currentInternalPlace;
    
    public TabletAndDesktopEventView(SailingServiceAsync sailingService, EventDTO event, EventNavigationTabs navigationTab, List<RaceGroupDTO> raceGroups, String leaderboardName,   
            Timer timerForClientServerOffset, HomePlacesNavigator navigator) {
        this.event = event;
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> regattaStructure = getRegattaStructure(event, raceGroups);

        eventHeader = new EventHeader(event, navigator, this);
        eventRegattaList = new EventRegattaList(event, regattaStructure, timerForClientServerOffset, navigator, this);
        eventRegattaRaces = new EventRegattaRaces(event, timerForClientServerOffset, navigator, this);
        eventOverview = new EventOverview(event, this);
        eventSchedule = new EventSchedule(event, this);
        eventMedia = new EventMedia(event, this);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageElements = Arrays.asList(new Widget[] { eventOverview, eventRegattaList, eventRegattaRaces, eventMedia, eventSchedule });

        Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO> selectedRegatta = null;
        if(leaderboardName != null) {
            selectedRegatta = regattaStructure.get(leaderboardName);
        }
        
        // in case we only have one regatta/leaderboard we go directly to the 'races' page
        if(selectedRegatta == null && regattaStructure.size() == 1) {
            selectedRegatta = regattaStructure.get(0);
        }

        if(selectedRegatta != null && (navigationTab == EventNavigationTabs.Regatta || !event.isFakeSeries())) {
            goToRegattaRaces(selectedRegatta.getC(), selectedRegatta.getB(), selectedRegatta.getA());
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

    @Override
    public void updateEventRaceStates(List<RegattaOverviewEntryDTO> racesStateEntries) {
        
    }

    @Override
    public void goToOverview() {
        setVisibleEventElement(eventOverview);
        eventHeader.setFullsizeHeader();
        
        currentInternalPlace = new EventPlace(event.id.toString(), EventNavigationTabs.Overview, null);
    }

    @Override
    public void goToRegattas() {
        setVisibleEventElement(eventRegattaList);
        eventHeader.setFullsizeHeader();
        
        currentInternalPlace = new EventPlace(event.id.toString(), EventNavigationTabs.Regattas, null);
    }

    @Override
    public void goToRegattaRaces(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard, RaceGroupDTO raceGroup) {
        eventRegattaRaces.setRaces(leaderboardGroup, false, leaderboard, raceGroup);
        eventHeader.setCompactHeader();
        setVisibleEventElement(eventRegattaRaces);

        currentInternalPlace = new EventPlace(event.id.toString(), EventNavigationTabs.Regatta, leaderboard.name);
    }

    @Override
    public void goToSchedule() {
        setVisibleEventElement(eventSchedule);
        eventHeader.setFullsizeHeader();
        
        currentInternalPlace = new EventPlace(event.id.toString(), EventNavigationTabs.Schedule, null);
    }

    @Override
    public void goToMedia() {
        setVisibleEventElement(eventMedia);
        eventHeader.setFullsizeHeader();
        
        currentInternalPlace = new EventPlace(event.id.toString(), EventNavigationTabs.Media, null);
    }

    public String getRaceViewerURL(StrippedLeaderboardDTO leaderboard, RaceDTO race) {
        RegattaAndRaceIdentifier raceIdentifier = race.getRaceIdentifier();
        return EntryPointLinkFactory.createRaceBoardLink(createRaceBoardLinkParameters(leaderboard.name, raceIdentifier));
    }
    
    @Override
    public void openLeaderboardViewer(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        String link = EntryPointLinkFactory.createLeaderboardLink(createLeaderboardLinkParameters(leaderboardGroup, leaderboard));
        Window.open(link, "_blank", "");
    }

    @Override
    public void openOverallLeaderboardViewer(LeaderboardGroupDTO leaderboardGroup) {
        String link = EntryPointLinkFactory.createLeaderboardLink(createOverallLeaderboardLinkParameters(leaderboardGroup));
        Window.open(link, "_blank", "");
    }

    private Map<String, String> createOverallLeaderboardLinkParameters(LeaderboardGroupDTO leaderboardGroup) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("eventId", event.id.toString());
        linkParams.put("name", leaderboardGroup.getName() + " " + LeaderboardNameConstants.OVERALL);
        linkParams.put("showRaceDetails", "true");
        if(leaderboardGroup.getDisplayName() != null) {
            linkParams.put("displayName", leaderboardGroup.getDisplayName());
        }
        linkParams.put("leaderboardGroupName", leaderboardGroup.getName());
        return linkParams;
    }

    private Map<String, String> createRaceBoardLinkParameters(String leaderboardName, RegattaAndRaceIdentifier raceIdentifier) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("eventId", event.id.toString());
        linkParams.put("leaderboardName", leaderboardName);
        linkParams.put("raceName", raceIdentifier.getRaceName());
        // TODO this must only be forwarded if there is a logged-on user
//        linkParams.put(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, "true");
        linkParams.put(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, "true");
        linkParams.put(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_NAVIGATION_PANEL, "true");
        linkParams.put("regattaName", raceIdentifier.getRegattaName());
        return linkParams;
    }

    private Map<String, String> createLeaderboardLinkParameters(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        Map<String, String> linkParams = new HashMap<String, String>();
        linkParams.put("eventId", event.id.toString());
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

    private Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> getRegattaStructure(EventDTO event, List<RaceGroupDTO> raceGroups) {
        Map<String, Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>> result = new HashMap<>();
        Map<String, RaceGroupDTO> raceGroupsMap = new HashMap<>();
        for (RaceGroupDTO raceGroup: raceGroups) {
            raceGroupsMap.put(raceGroup.getName(), raceGroup);
        }            
        
        for (LeaderboardGroupDTO leaderboardGroup : event.getLeaderboardGroups()) {
            for(StrippedLeaderboardDTO leaderboard: leaderboardGroup.getLeaderboards()) {
                String leaderboardName = leaderboard.name;
                result.put(leaderboardName, new Triple<RaceGroupDTO, StrippedLeaderboardDTO, LeaderboardGroupDTO>(raceGroupsMap.get(leaderboardName),
                        leaderboard, leaderboardGroup));
            }
        }
        return result;
    }

}
