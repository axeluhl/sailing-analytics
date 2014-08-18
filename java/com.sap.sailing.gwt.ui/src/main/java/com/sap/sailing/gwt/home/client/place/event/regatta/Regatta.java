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
import com.sap.sailing.gwt.home.client.BoatClassImageResolver;
import com.sap.sailing.gwt.home.client.BoatClassImageResources;
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

    private final boolean isNavigatable;
    private final EventDTO event;
    private final Timer timerForClientServerOffset;
    private final EventPageNavigator pageNavigator;
    private final PlaceNavigator placeNavigator;
    private StrippedLeaderboardDTO leaderboard;
    private RaceGroupDTO raceGroup;
    private final List<RegattaPhase> phasesElements;
    private LeaderboardGroupDTO leaderboardGroup;

    @UiField DivElement regattaDiv;
    @UiField HeadingElement regattaNameHeading1;
    @UiField HeadingElement regattaNameHeading2;
    @UiField DivElement leaderboardGroupName;
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
    @UiField AnchorElement leaderboardLink;
//    @UiField DivElement liveRaceInfosPerFleetPanel;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank1;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank2;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank3;
    
    @UiField DivElement regattaImageWithoutLink;
    @UiField AnchorElement regattaImageWithLink;
    @UiField AnchorElement regattaNameLink;

    @UiField Image boatClassImage1;
    @UiField Image boatClassImage2;
    
    public Regatta(EventDTO event, boolean isNavigatable, Timer timerForClientServerOffset, PlaceNavigator placeNavigator, EventPageNavigator pageNavigator) {
        this.event = event;
        this.isNavigatable = isNavigatable;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.placeNavigator = placeNavigator;
        this.pageNavigator = pageNavigator;
        
        phasesElements = new ArrayList<RegattaPhase>();
        
//        competitorWithRank1 = new RegattaCompetitor(1, null);
//        competitorWithRank2 = new RegattaCompetitor(2, null);
//        competitorWithRank3 = new RegattaCompetitor(3, null);
        
        RegattaResources.INSTANCE.css().ensureInjected();
        StyleInjector.injectAtEnd("@media (min-width: 50em) { "+RegattaResources.INSTANCE.largeCss().getText()+"}");

        initWidget(uiBinder.createAndBindUi(this));

        if(isNavigatable) {
            regattaDiv.setAttribute("data-rendermode", "link");
            regattaImageWithoutLink.getStyle().setDisplay(Display.NONE);
            regattaNameHeading2.getStyle().setDisplay(Display.NONE);
        } else {
            regattaImageWithLink.getStyle().setDisplay(Display.NONE);
            regattaNameLink.getStyle().setDisplay(Display.NONE);
        }
        
        registerEvents();
    }

    public void setData(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard, RaceGroupDTO raceGroup) {
        this.raceGroup = raceGroup;
        this.leaderboard = leaderboard;
        this.leaderboardGroup = leaderboardGroup;
        
        boolean hasLiveRace = leaderboard.hasLiveRace(timerForClientServerOffset.getLiveTimePointInMillis());
        if(!hasLiveRace) {
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        
        // boolean isFinished... TODO
        Image boatClassImageToSet = isNavigatable ? boatClassImage1 : boatClassImage2;  
        if(raceGroup.boatClass != null) {
            boatClassImageToSet.setResource(BoatClassImageResolver.getBoatClassIconResource(raceGroup.boatClass));
        } else {
            boatClassImageToSet.setResource(BoatClassImageResources.INSTANCE.genericBoatClass());
        }
        
        String regattaDisplayName = leaderboard.displayName != null ? leaderboard.displayName : leaderboard.name;
        regattaNameHeading1.setInnerText(regattaDisplayName);
        regattaNameHeading2.setInnerText(regattaDisplayName);
        if(leaderboardGroup.getName() != null) {
            leaderboardGroupName.setInnerText(LongNamesUtil.shortenLeaderboardGroupName(event.getName(), leaderboardGroup.getName()));
            leaderboardGroupName.setAttribute("data-labeltype", "group1");
        } else {
            leaderboardGroupName.getStyle().setDisplay(Display.NONE);
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
                    RegattaPhase regattaPhase = new RegattaPhase(series, leaderboard, timerForClientServerOffset); 
                    regattaPhasesPanel.appendChild(regattaPhase.getElement());
                    phasesElements.add(regattaPhase);
                }
            }
        }
        
        racesCount.setInnerText(String.valueOf(leaderboard.getRacesCount()));
        trackedRacesCount.setInnerText(String.valueOf(leaderboard.getTrackedRacesCount()));
    }

    private void registerEvents() {
        Event.sinkEvents(leaderboardLink, Event.ONCLICK);
        Event.setEventListener(leaderboardLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event browserEvent) {
                switch (DOM.eventGetType(browserEvent)) {
                    case Event.ONCLICK:
                        placeNavigator.goToLeaderboard(event.id.toString(), leaderboard.name, event.getBaseURL(), event.isOnRemoteServer());
                        // pageNavigator.openLeaderboardViewer(null, leaderboard);
                        break;
                }
            }
        });

        if(isNavigatable) {
            Event.sinkEvents(regattaImageWithLink, Event.ONCLICK);
            Event.setEventListener(regattaImageWithLink, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    switch (DOM.eventGetType(event)) {
                        case Event.ONCLICK:
                            pageNavigator.goToRegattaRaces(leaderboardGroup, leaderboard, raceGroup);
                            break;
                    }
                }
            });

            Event.sinkEvents(regattaNameLink, Event.ONCLICK);
            Event.setEventListener(regattaNameLink, new EventListener() {
                @Override
                public void onBrowserEvent(Event event) {
                    switch (DOM.eventGetType(event)) {
                        case Event.ONCLICK:
                            pageNavigator.goToRegattaRaces(leaderboardGroup, leaderboard, raceGroup);
                            break;
                    }
                }
            });
        }
    }
}
