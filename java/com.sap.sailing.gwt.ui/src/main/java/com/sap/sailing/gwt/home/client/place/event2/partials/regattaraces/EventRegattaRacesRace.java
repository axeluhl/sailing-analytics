package com.sap.sailing.gwt.home.client.place.event2.partials.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.place.event2.EventView.Presenter;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaRacesRace extends Composite {
    private static EventRegattaRacesRaceUiBinder uiBinder = GWT.create(EventRegattaRacesRaceUiBinder.class);

    private enum SimpleRaceStates { NOT_TRACKED, TRACKED, TRACKED_AND_LIVE, TRACKED_BUT_NOT_SCHEDULED };
    
    interface EventRegattaRacesRaceUiBinder extends UiBinder<Widget, EventRegattaRacesRace> {
    }

    @UiField DivElement fleetColor;
    @UiField SpanElement raceName;
    @UiField SpanElement raceName2;
    @UiField SpanElement raceState;
    @UiField SpanElement raceTime;
    @UiField SpanElement averageRaceWind;
    
    @UiField DivElement raceNotTrackedDiv;
    @UiField DivElement raceNotScheduledDiv;
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
    
    @UiField SpanElement currentLegNo;
    @UiField SpanElement totalLegsCount;
    
    private final DateTimeFormat raceTimeFormat = DateTimeFormat.getFormat("EEE, h:mm a");
    
    private final FleetDTO fleet;
    private final RaceColumnDTO raceColumn;
    private final RaceDTO race;

    private Element[] allConditionalElements;
    private final Presenter presenter;
    
    public EventRegattaRacesRace(StrippedLeaderboardDTO leaderboard, FleetDTO fleet, RaceColumnDTO raceColumn, EventView.Presenter presenter) {
        this.fleet = fleet;
        this.raceColumn = raceColumn;
        this.presenter = presenter;
        race = raceColumn.getRace(fleet);
        
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        allConditionalElements = new Element[] {raceWinnerDiv, raceLeaderDiv, watchRaceDiv, analyzeRaceDiv, raceNotTrackedDiv, raceNotScheduledDiv,
                raceFeaturesDiv, legProgressDiv, raceFlagDiv, windStatusDiv };

        if(fleet.getColor() != null) {
            fleetColor.getStyle().setBackgroundColor(fleet.getColor().getAsHtml());
        }
        
        raceName.setInnerText(raceColumn.getName());
        raceName2.setInnerText(raceColumn.getName());

        if(race != null && race.trackedRace != null) {
            String raceViewerURL = presenter.getRaceViewerURL(leaderboard, race);
            analyzeRaceLink.setHref(raceViewerURL);
            watchRaceLink.setHref(raceViewerURL);
        }
        
        updateUI();    
    }
    
    private SimpleRaceStates getSimpleRaceState() {
        SimpleRaceStates simpleRaceState = SimpleRaceStates.NOT_TRACKED;
        
        if(race != null && race.trackedRace != null && race.trackedRace.hasGPSData && race.trackedRace.hasWindData) {
            simpleRaceState = SimpleRaceStates.TRACKED;
            if(isLive()) {
                simpleRaceState = SimpleRaceStates.TRACKED_AND_LIVE;
                if(race.startOfRace == null) {
                    simpleRaceState = SimpleRaceStates.TRACKED_BUT_NOT_SCHEDULED;
                }                    
            }
        }
        return simpleRaceState;
    }

    private boolean isLive() {
        return raceColumn.isLive(fleet, presenter.getTimerForClientServerOffset().getLiveTimePointInMillis());
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
            case TRACKED_BUT_NOT_SCHEDULED:
                showElement(raceNotScheduledDiv);
                break;
            case TRACKED_AND_LIVE:
                showElement(watchRaceDiv);

                if(race.startOfRace != null) {
                    raceTime.setInnerText(raceTimeFormat.format(race.startOfRace));
                }
                if(race.trackedRaceStatistics.hasLegProgressData) {
                    showElement(legProgressDiv);
                    currentLegNo.setInnerText(String.valueOf(race.trackedRaceStatistics.currentLegNo));
                    totalLegsCount.setInnerText(String.valueOf(race.trackedRaceStatistics.totalLegsCount));
                }
                if(race.trackedRaceStatistics.hasLeaderOrWinnerData && race.trackedRaceStatistics.leaderOrWinner != null) {
                    showElement(raceLeaderDiv);
                    updateWinnerOrLeader(raceLeader, race.trackedRaceStatistics.leaderOrWinner);
                }

                break;
            case TRACKED:
                showElement(analyzeRaceDiv);
                
                if(race.trackedRaceStatistics.hasLeaderOrWinnerData && race.trackedRaceStatistics.leaderOrWinner != null) {
                    showElement(raceWinnerDiv);
                    updateWinnerOrLeader(raceWinner, race.trackedRaceStatistics.leaderOrWinner);
                }
                if(race.startOfRace != null) {
                    raceTime.setInnerText(raceTimeFormat.format(race.startOfRace));
                }
                showElement(raceFeaturesDiv);
                updateRaceFeatures();

                break;
        }
    }
    
    private void updateWinnerOrLeader(SpanElement winnerOrLeaderElement, CompetitorDTO winnerOrLeader) {
        String winnerOrLeaderName = winnerOrLeader.getName();
        if(winnerOrLeader.getThreeLetterIocCountryCode() != null) {
            winnerOrLeaderName += " (" + winnerOrLeader.getThreeLetterIocCountryCode() + ")";
        }
        winnerOrLeaderElement.setInnerText(winnerOrLeaderName);
    }
        
    private void updateRaceFeatures() {
        if(!race.trackedRaceStatistics.hasGPSData) {
            featureGPS.addClassName(EventRegattaRacesResources.INSTANCE.css().eventregattarace_featureunavailable());
        }
        if(!race.trackedRaceStatistics.hasMeasuredWindData) {
            featureWind.addClassName(EventRegattaRacesResources.INSTANCE.css().eventregattarace_featureunavailable());
        }
        if(!race.trackedRaceStatistics.hasVideoData) {
            featureVideo.addClassName(EventRegattaRacesResources.INSTANCE.css().eventregattarace_featureunavailable());
        } else {
            featureVideo.setTitle(String.valueOf(race.trackedRaceStatistics.videoTracksCount));
        }
        if(!race.trackedRaceStatistics.hasAudioData) {
            featureAudio.addClassName(EventRegattaRacesResources.INSTANCE.css().eventregattarace_featureunavailable());
        } else {
            featureAudio.setTitle(String.valueOf(race.trackedRaceStatistics.audioTracksCount));
        }
    }
    
    private void hideElement(Element el) {
        el.getStyle().setVisibility(Visibility.HIDDEN);
        el.getStyle().setDisplay(Display.NONE);
    }

    private void showElement(Element el) {
        el.getStyle().setVisibility(Visibility.VISIBLE);
        el.getStyle().setDisplay(Display.INLINE_BLOCK);
    }
}
