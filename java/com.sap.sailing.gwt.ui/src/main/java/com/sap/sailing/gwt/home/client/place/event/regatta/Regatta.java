package com.sap.sailing.gwt.home.client.place.event.regatta;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.common.client.BoatClassImageResources;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class Regatta extends Composite {
    private static RegattaUiBinder uiBinder = GWT.create(RegattaUiBinder.class);

    interface RegattaUiBinder extends UiBinder<Widget, Regatta> {
    }

    private final EventDTO event;
    private final Timer timerForClientServerOffset;
    private final EventPageNavigator pageNavigator;
    private final PlaceNavigator placeNavigator;
    private StrippedLeaderboardDTO leaderboard;
    private RaceGroupDTO raceGroup;
    private final List<RegattaPhase> phasesElements;
    private LeaderboardGroupDTO leaderboardGroup;
    private final boolean isSingleView;

    @UiField DivElement regattaDiv;
    @UiField HeadingElement regattaNameHeading;
    @UiField DivElement leaderboardGroupName;
    @UiField DivElement leaderboardGroupNameDiv;
//  @UiField SpanElement scheduledStart;
    @UiField DivElement regattaPhasesPanel;
    @UiField DivElement regattaPhasesInfoDiv;
    
    @UiField SpanElement competitorsCount;
    @UiField DivElement competitorsCountDiv;
    @UiField SpanElement racesCount;
    @UiField SpanElement trackedRacesCount;
    @UiField DivElement isLiveDiv;
//    @UiField TableElement isLiveDiv2;
//    @UiField DivElement isFinishedDiv;
//    @UiField DivElement isFinishedDiv2;
//    @UiField DivElement isScheduledDiv;
//    @UiField AnchorElement leaderboardLink;
//    @UiField DivElement liveRaceInfosPerFleetPanel;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank1;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank2;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank3;

    @UiField AnchorElement regattaDetailsLink;
    @UiField AnchorElement leaderboardLink;
    
    @UiField Image boatClassImage;
    
    public Regatta(EventDTO event, Timer timerForClientServerOffset, boolean isSingleView, PlaceNavigator placeNavigator, EventPageNavigator pageNavigator) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.isSingleView = isSingleView;
        this.placeNavigator = placeNavigator;
        this.pageNavigator = pageNavigator;
        
        phasesElements = new ArrayList<RegattaPhase>();
        
//        competitorWithRank1 = new RegattaCompetitor(1, null);
//        competitorWithRank2 = new RegattaCompetitor(2, null);
//        competitorWithRank3 = new RegattaCompetitor(3, null);
        
        RegattaResources.INSTANCE.css().ensureInjected();
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+RegattaResources.INSTANCE.largeCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(LeaderboardGroupDTO leaderboardGroup, boolean hasMultipleLeaderboardGroups, StrippedLeaderboardDTO leaderboard,
            RaceGroupDTO raceGroup) {
        this.raceGroup = raceGroup;
        this.leaderboard = leaderboard;
        this.leaderboardGroup = leaderboardGroup;

        registerEvents(leaderboardGroup);

        boolean hasLiveRace = leaderboard.hasLiveRace(timerForClientServerOffset.getLiveTimePointInMillis());
        if(!hasLiveRace) {
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        
        // boolean isFinished... TODO
        if(raceGroup.boatClass != null) {
            boatClassImage.setResource(BoatClassImageResolver.getBoatClassIconResource(raceGroup.boatClass));
        } else {
            boatClassImage.setResource(BoatClassImageResources.INSTANCE.genericBoatClass());
        }
        
        String regattaDisplayName = leaderboard.displayName != null ? leaderboard.displayName : leaderboard.name;
        regattaNameHeading.setInnerText(regattaDisplayName);
        if(hasMultipleLeaderboardGroups && leaderboardGroup.getName() != null) {
            leaderboardGroupName.setInnerText(LongNamesUtil.shortenLeaderboardGroupName(event.getName(), leaderboardGroup.getName()));
            leaderboardGroupName.setAttribute("data-labeltype", "group1");
        } else {
            leaderboardGroupNameDiv.getStyle().setDisplay(Display.NONE);
        }
        
        if(leaderboard.competitorsCount > 0) {
            competitorsCount.setInnerText(String.valueOf(leaderboard.competitorsCount));
        } else {
            competitorsCountDiv.getStyle().setDisplay(Display.NONE);
        }
        setRegattaProgress(leaderboard);
    }
    
    private void setRegattaProgress(StrippedLeaderboardDTO leaderboard) {
        // clear first 
        regattaPhasesPanel.removeAllChildren();
        phasesElements.clear();

        if(raceGroup != null) {
            if(raceGroup.getSeries().size() == 0) {
                regattaPhasesInfoDiv.getStyle().setDisplay(Display.NONE);
            } else {
                for(RaceGroupSeriesDTO series: raceGroup.getSeries()) {
                    RegattaPhase regattaPhase = new RegattaPhase(series, leaderboardGroup,
                            leaderboard, raceGroup, pageNavigator, timerForClientServerOffset); 
                    regattaPhasesPanel.appendChild(regattaPhase.getElement());
                    phasesElements.add(regattaPhase);
                }
            }
        }
        
        racesCount.setInnerText(String.valueOf(leaderboard.getRacesCount()));
        trackedRacesCount.setInnerText(String.valueOf(leaderboard.getTrackedRacesCount()));
    }

    private void registerEvents(final LeaderboardGroupDTO leaderboardGroup) {
        if(isSingleView) {
            regattaDetailsLink.getStyle().setDisplay(Display.NONE);

            Event.sinkEvents(leaderboardLink, Event.ONCLICK);
            Event.setEventListener(leaderboardLink, new EventListener() {
                @Override
                public void onBrowserEvent(Event browserEvent) {
                    switch (DOM.eventGetType(browserEvent)) {
                        case Event.ONCLICK:
                            placeNavigator.goToLeaderboard(event.id.toString(), leaderboard.name, event.getBaseURL(), event.isOnRemoteServer());
                            break;
                    }
                }
            });
        } else {
            leaderboardLink.getStyle().setDisplay(Display.NONE);

            Event.sinkEvents(regattaDetailsLink, Event.ONCLICK);
            Event.setEventListener(regattaDetailsLink, new EventListener() {
                @Override
                public void onBrowserEvent(Event browserEvent) {
                    switch (DOM.eventGetType(browserEvent)) {
                        case Event.ONCLICK:
                            pageNavigator.goToRegattaRaces(leaderboardGroup, leaderboard, raceGroup);
                            break;
                    }
                }
            });
        }
    }
}
