package com.sap.sailing.gwt.home.client.place.event.partials;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.common.client.BoatClassImageResources;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.EventView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class Regatta extends Composite {
    private static RegattaUiBinder uiBinder = GWT.create(RegattaUiBinder.class);

    interface RegattaUiBinder extends UiBinder<Widget, Regatta> {
    }

    private final EventViewDTO event;
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

    @UiField Anchor regattaDetailsLink;
    
    @UiField Image boatClassImage;

    private Presenter presenter;

    private PlaceNavigation<RegattaRacesPlace> regattaNavigation;
    
    public Regatta(boolean isSingleView, EventView.Presenter presenter) {
        this.event = presenter.getCtx().getEventDTO();
        this.isSingleView = isSingleView;
        this.presenter = presenter;
        
        phasesElements = new ArrayList<RegattaPhase>();

        RegattaResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(LeaderboardGroupDTO leaderboardGroup, boolean hasMultipleLeaderboardGroups, StrippedLeaderboardDTO leaderboard,
            RaceGroupDTO raceGroup) {
        this.raceGroup = raceGroup;
        this.leaderboardGroup = leaderboardGroup;

        if(isSingleView) {
            regattaDetailsLink.setVisible(false);
        } else {
            regattaNavigation = presenter.getRegattaNavigation(leaderboard.regattaName);
            regattaDetailsLink.setHref(regattaNavigation.getTargetUrl());
        }

        boolean hasLiveRace = leaderboard.hasLiveRace(presenter.getTimerForClientServerOffset().getLiveTimePointInMillis());
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
            leaderboardGroupName.setInnerText(LongNamesUtil.shortenLeaderboardGroupName(event.getDisplayName(), leaderboardGroup.getName()));
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
                            leaderboard, raceGroup, presenter); 
                    regattaPhasesPanel.appendChild(regattaPhase.getElement());
                    phasesElements.add(regattaPhase);
                }
            }
        }
        
        racesCount.setInnerText(String.valueOf(leaderboard.getRacesCount()));
        trackedRacesCount.setInnerText(String.valueOf(leaderboard.getTrackedRacesCount()));
    }

    @UiHandler("regattaDetailsLink")
    void regattaDetailsLinkClicked(ClickEvent e) {
        handleClickEvent(e, regattaNavigation);
    }
    
    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> place) {
        if (LinkUtil.handleLinkClick((Event) e.getNativeEvent())) {
            place.goToPlace();
            e.preventDefault();
         }
    }
}
