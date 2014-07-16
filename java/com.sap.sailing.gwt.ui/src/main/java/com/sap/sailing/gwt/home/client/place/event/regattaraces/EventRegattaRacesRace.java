package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRacesRace extends Composite {
    private static EventRegattaRacesRaceUiBinder uiBinder = GWT.create(EventRegattaRacesRaceUiBinder.class);

    private enum SimpleRaceStates { NOT_TRACKED, TRACKED_AND_NOT_LIVE, TRACKED_AND_LIVE };
    
    interface EventRegattaRacesRaceUiBinder extends UiBinder<Widget, EventRegattaRacesRace> {
    }

    @UiField DivElement fleetColor;
    @UiField SpanElement raceName;
    @UiField SpanElement raceName2;
    @UiField SpanElement raceState;
    @UiField SpanElement raceTime;
    @UiField SpanElement averageRaceWind;
    
    @UiField DivElement raceNotTrackedDiv;
    @UiField DivElement watchRaceDiv;
    @UiField DivElement analyzeRaceDiv;
    @UiField Anchor watchRaceLink;
    @UiField Anchor analyzeRaceLink;
    
    @UiField DivElement raceWinnerDiv;
    @UiField SpanElement raceWinner;
    @UiField DivElement raceLeaderDiv;
    @UiField SpanElement raceLeader;

    @UiField DivElement raceFeaturesDiv;
    @UiField DivElement legProgressDiv;
    @UiField DivElement raceFlagDiv;
    @UiField DivElement windStatusDiv;
    
    @UiField DivElement featureGPS;
    @UiField DivElement featureWind;
    @UiField DivElement featureAudio;
    @UiField DivElement featureVideo;
    
    // local_res.css.eventregattarace_featureunavailable
    
    private final DateTimeFormat raceTimeFormat = DateTimeFormat.getFormat("EEE, h:mm a");

    private final TextMessages textMessages = GWT.create(TextMessages.class);
    
    private final StrippedLeaderboardDTO leaderboard;
    private final FleetDTO fleet;
    private final RaceColumnDTO raceColumn;
    private final EventPageNavigator pageNavigator;
    private final Timer timerForClientServerOffset;

    private Element[] allConditionalElements;
    
    public EventRegattaRacesRace(StrippedLeaderboardDTO leaderboard, FleetDTO fleet, RaceColumnDTO raceColumn, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.leaderboard = leaderboard;
        this.fleet = fleet;
        this.raceColumn = raceColumn;
        this.pageNavigator = pageNavigator;
        this.timerForClientServerOffset = timerForClientServerOffset;
        
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        allConditionalElements = new Element[] {raceWinnerDiv, raceLeaderDiv, watchRaceDiv, analyzeRaceDiv, raceNotTrackedDiv,
                raceFeaturesDiv, legProgressDiv, raceFlagDiv, windStatusDiv };

        fleetColor.getStyle().setBackgroundColor(fleet.getColor().getAsHtml());
        
        raceName.setInnerText(raceColumn.getName());
        raceName2.setInnerText(raceColumn.getName());
        
//        {#if race.isFinished}
//        <ui:text from='{i18n.eventRegattaRaceFinished}'/><span ui:field="endOfRaceTime" />
//        {/if race.isFinished}
//        {#if race.isLive}
//        <ui:text from='{i18n.eventRegattaRaceStarted}'/> {race.startTime}
//        {/if race.isLive}
//        {#if race.isScheduled}
//        <ui:text from='{i18n.eventRegattaRaceScheduled}'/> {race.startTime}
//        {/if race.isScheduled}
        
        updateUI();    
    }

    private SimpleRaceStates getSimpleRaceState() {
        SimpleRaceStates simpleRaceState = SimpleRaceStates.NOT_TRACKED;
        
        RaceDTO race = raceColumn.getRace(fleet);
        if(race != null && race.trackedRace != null) {
            simpleRaceState = isLive() ? SimpleRaceStates.TRACKED_AND_LIVE : SimpleRaceStates.TRACKED_AND_NOT_LIVE; 
//            String startOfTrackingTime = raceTimeFormat.format(race.trackedRace.startOfTracking);
        }
        return simpleRaceState;
    }

    private boolean isLive() {
        return raceColumn.isLive(fleet, timerForClientServerOffset.getLiveTimePointInMillis());
    }
    
    private void updateUI() {
        averageRaceWind.setInnerText("tbd.");
        
        for(Element el: allConditionalElements) {
            hideElement(el);
        }
        
        switch(getSimpleRaceState()) {
            case NOT_TRACKED:
                showElement(raceNotTrackedDiv);
                break;
            case TRACKED_AND_LIVE:
                showElement(watchRaceDiv);
                showElement(raceLeaderDiv);
                showElement(legProgressDiv);
                break;
            case TRACKED_AND_NOT_LIVE:
                showElement(analyzeRaceDiv);
                showElement(raceWinnerDiv);
                showElement(raceFeaturesDiv);
                break;
        }
    }
    
    @UiHandler("watchRaceLink")
    public void watchRaceClicked(ClickEvent e) {
        openRaceViewer();
    }

    @UiHandler("analyzeRaceLink")
    public void analyzeRaceClicked(ClickEvent e) {
        openRaceViewer();
    }

    private void hideElement(Element el) {
        el.getStyle().setVisibility(Visibility.HIDDEN);
        el.getStyle().setDisplay(Display.NONE);
    }

    private void showElement(Element el) {
        el.getStyle().setVisibility(Visibility.VISIBLE);
        el.getStyle().setDisplay(Display.INLINE_BLOCK);
    }

    private void openRaceViewer() {
        RaceDTO race = raceColumn.getRace(fleet);
        if(race != null && race.trackedRace != null) {
            pageNavigator.openRaceViewer(leaderboard, race);
        }
    }
}
