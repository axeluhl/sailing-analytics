package com.sap.sailing.gwt.home.client.place.event.regatta;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
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

    @SuppressWarnings("unused")
    private final EventDTO event;
    private final Timer timerForClientServerOffset;
    private final EventPageNavigator pageNavigator;
    private StrippedLeaderboardDTO leaderboard;
    private RaceGroupDTO raceGroup;
    private final List<RegattaPhase> phasesElements;
    
    @SuppressWarnings("unused")
    private LeaderboardGroupDTO leaderboardGroup;

    @UiField DivElement regattaDiv;
    @UiField HeadingElement regattaName1;
    @UiField HeadingElement regattaName2;
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
    @UiField DivElement isFinishedDiv;
//    @UiField DivElement isFinishedDiv2;
//    @UiField DivElement isScheduledDiv;
    @UiField AnchorElement leaderboardLink;
//    @UiField DivElement liveRaceInfosPerFleetPanel;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank1;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank2;
//    @UiField(provided=true) RegattaCompetitor competitorWithRank3;
    
    @UiField DivElement regattaImageWithoutLink;
    @UiField AnchorElement regattaImageWithLink;

    @UiField DivElement regattaNameWithoutLink;
    @UiField AnchorElement regattaNameWithLink;

    public Regatta(EventDTO event, boolean isNavigatable, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.pageNavigator = pageNavigator;
        
        phasesElements = new ArrayList<RegattaPhase>();
        
//        competitorWithRank1 = new RegattaCompetitor(1, null);
//        competitorWithRank2 = new RegattaCompetitor(2, null);
//        competitorWithRank3 = new RegattaCompetitor(3, null);
        
        RegattaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        if(isNavigatable) {
            regattaDiv.setAttribute("data-rendermode", "link");
            regattaImageWithoutLink.getStyle().setDisplay(Display.NONE);
            regattaNameWithoutLink.getStyle().setDisplay(Display.NONE);
        } else {
            regattaImageWithLink.getStyle().setDisplay(Display.NONE);
            regattaNameWithLink.getStyle().setDisplay(Display.NONE);
        }
        
        registerEvents();
    }

    public void setData(RaceGroupDTO raceGroup, StrippedLeaderboardDTO leaderboard, LeaderboardGroupDTO leaderboardGroup) {
        this.raceGroup = raceGroup;
        this.leaderboard = leaderboard;
        this.leaderboardGroup = leaderboardGroup;
        
        boolean hasLiveRace = leaderboard.hasLiveRace(timerForClientServerOffset.getLiveTimePointInMillis());
        if(!hasLiveRace) {
            isLiveDiv.getStyle().setDisplay(Display.NONE);
        }
        
        boolean isFinished = !hasLiveRace;
        if(!isFinished) {
            isFinishedDiv.getStyle().setDisplay(Display.NONE);
//            isFinishedDiv2.getStyle().setDisplay(Display.NONE);
        }
        
        String regattaDisplayName = leaderboard.displayName != null ? leaderboard.displayName : leaderboard.name;
        regattaName1.setInnerText(regattaDisplayName);
        regattaName2.setInnerText(regattaDisplayName);
        if(raceGroup.leaderboardGroupName != null) {
            leaderboardGroupName.setInnerText(raceGroup.leaderboardGroupName);
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
        
        if(raceGroup.getSeries().size() == 0) {
            regattaPhasesInfoDiv.getStyle().setDisplay(Display.NONE);
        } else {
            for(RaceGroupSeriesDTO series: raceGroup.getSeries()) {
                RegattaPhase regattaPhase = new RegattaPhase(series); 
                regattaPhasesPanel.appendChild(regattaPhase.getElement());
                phasesElements.add(regattaPhase);
            }
        }
        
        racesCount.setInnerText(String.valueOf(leaderboard.getRaceColumnsCount()));
        trackedRacesCount.setInnerText(String.valueOf(leaderboard.getTrackedRacesCount()));
    }

    private void registerEvents() {
        Event.sinkEvents(leaderboardLink, Event.ONCLICK);
        Event.setEventListener(leaderboardLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                pageNavigator.openLeaderboardViewer(null, leaderboard);
            }
        });

        Event.sinkEvents(regattaImageWithLink, Event.ONCLICK);
        Event.setEventListener(regattaImageWithLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                pageNavigator.goToRegattaRaces(raceGroup, leaderboard);
            }
        });

        Event.sinkEvents(regattaNameWithLink, Event.ONCLICK);
        Event.setEventListener(regattaNameWithLink, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                pageNavigator.goToRegattaRaces(raceGroup, leaderboard);
            }
        });
    }
}
