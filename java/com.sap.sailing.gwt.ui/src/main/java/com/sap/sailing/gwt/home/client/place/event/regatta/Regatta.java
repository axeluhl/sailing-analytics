package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
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

    @SuppressWarnings("unused")
    private LeaderboardGroupDTO leaderboardGroup;

    @UiField SpanElement regattaName1;
    @UiField SpanElement regattaName2;
    @UiField DivElement leaderboardGroupName;
    @UiField SpanElement scheduledStart;
    @UiField DivElement regattaPhasesPanel;
    @UiField SpanElement competitorsCount;
    @UiField SpanElement racesCount;
    @UiField SpanElement trackedRacesCount;
    @UiField DivElement isLiveDiv;
    @UiField TableElement isLiveDiv2;
    @UiField DivElement isFinishedDiv;
    @UiField DivElement isFinishedDiv2;
    @UiField DivElement isScheduledDiv;
    @UiField Anchor leaderboardLink;
    @UiField HTMLPanel liveRaceInfosPerFleetPanel;
    @UiField(provided=true) RegattaCompetitor competitorWithRank1;
    @UiField(provided=true) RegattaCompetitor competitorWithRank2;
    @UiField(provided=true) RegattaCompetitor competitorWithRank3;
    
    @UiField HTMLPanel regattaImage;
    @UiField Anchor regattaImageWithLink;

    @UiField HTMLPanel regattaNameDiv;
    @UiField Anchor regattaNameWithLink;

    public Regatta(EventDTO event, boolean isNavigatable, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.pageNavigator = pageNavigator;
        
        competitorWithRank1 = new RegattaCompetitor(1, null);
        competitorWithRank2 = new RegattaCompetitor(2, null);
        competitorWithRank3 = new RegattaCompetitor(3, null);
        
        RegattaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        regattaImageWithLink.getElement().setAttribute("data-rendermode", "link");
        regattaImageWithLink.setVisible(isNavigatable);
        regattaImage.setVisible(!isNavigatable);

        regattaNameWithLink.getElement().setAttribute("data-rendermode", "link");
        regattaNameWithLink.setVisible(isNavigatable);
        regattaNameDiv.setVisible(!isNavigatable);
    }

    public void setData(RaceGroupDTO raceGroup, StrippedLeaderboardDTO leaderboard, LeaderboardGroupDTO leaderboardGroup) {
        this.raceGroup = raceGroup;
        this.leaderboard = leaderboard;
        this.leaderboardGroup = leaderboardGroup;
        
        boolean hasLiveRace = leaderboard.hasLiveRace(timerForClientServerOffset.getLiveTimePointInMillis());
        isLiveDiv.setAttribute("data-labeltype", hasLiveRace ? "live" : "");
        
        boolean isFinished = !hasLiveRace;
        if(!isFinished) {
            isFinishedDiv.getStyle().setDisplay(Display.NONE);
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
        
        if(leaderboard.rows != null) {
            competitorsCount.setInnerText(String.valueOf(leaderboard.rows.size()));
        } else {
            competitorsCount.getStyle().setVisibility(Visibility.HIDDEN);
        }
        racesCount.setInnerText("tbd");
        trackedRacesCount.setInnerText("tbd");
        
//        regattaPhasesPanel.add(xxx);
        setRegattaProgress();
    }
    
    private void setRegattaProgress() {
        for(RaceGroupSeriesDTO series: raceGroup.getSeries()) {
            RegattaPhase regattaPhase = new RegattaPhase(series); 
            regattaPhasesPanel.appendChild(regattaPhase.getElement());
        }
    }

    @UiHandler("leaderboardLink")
    public void goToLeaderboard(ClickEvent e) {
        pageNavigator.openLeaderboardViewer(null, leaderboard);
    }
    
    @UiHandler("regattaImageWithLink")
    public void goToRegattaRaces(ClickEvent e) {
        pageNavigator.goToRegattaRaces(raceGroup, leaderboard);
    }

    @UiHandler("regattaNameWithLink")
    public void goToRegattaRaces2(ClickEvent e) {
        pageNavigator.goToRegattaRaces(raceGroup, leaderboard);
    }
}
