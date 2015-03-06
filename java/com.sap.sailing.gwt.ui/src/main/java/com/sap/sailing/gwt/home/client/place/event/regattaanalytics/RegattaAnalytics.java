package com.sap.sailing.gwt.home.client.place.event.regattaanalytics;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.header.CompactEventHeader;
import com.sap.sailing.gwt.home.client.place.event.oldcompetitorcharts.OldCompetitorCharts;
import com.sap.sailing.gwt.home.client.place.event.oldleaderboard.OldLeaderboard;
import com.sap.sailing.gwt.home.client.place.event.regattaleaderboard.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace.RegattaNavigationTabs;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class RegattaAnalytics extends Composite implements LeaderboardUpdateListener {
    private static RegattaAnalyticsUiBinder uiBinder = GWT.create(RegattaAnalyticsUiBinder.class);

    interface RegattaAnalyticsUiBinder extends UiBinder<Widget, RegattaAnalytics> {
    }

    private static final HyperlinkImpl HYPERLINK_IMPL = GWT.create(HyperlinkImpl.class);

    @UiField OldLeaderboard oldLeaderboardComposite;
    @UiField OldCompetitorCharts oldCompetitorChartsComposite;
    @UiField(provided=true) CompactEventHeader eventHeader;
    
    @UiField SpanElement title;
    @UiField DivElement liveRaceDiv;

    @UiField HTMLPanel leaderboardTabPanel;
    @UiField HTMLPanel competitorChartsTabPanel;
    private HTMLPanel activeTabPanel;
    private Anchor activeAnchor;
    
    @UiField Anchor leaderboardAnchor;
    @UiField Anchor competitorChartsAnchor;

    private final PlaceNavigation<RegattaPlace> leaderboardNavigation; 
    private final PlaceNavigation<RegattaPlace> competitorChartsNavigation; 

    private RegattaAnalyticsDataManager regattaAnalyticsManager;
    private Timer autoRefreshTimer;
    private RegattaNavigationTabs navigationTab;
    
    public RegattaAnalytics(EventDTO event, String leaderboardName, RegattaNavigationTabs navigationTab, Timer timerForClientServerOffset, HomePlacesNavigator placesNavigator) {
        this.navigationTab = navigationTab;
        
        eventHeader = new CompactEventHeader(event, leaderboardName, placesNavigator);
    
        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        RegattaAnalyticsResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        title.setInnerText(leaderboardName);

        competitorChartsTabPanel.setVisible(false);
        leaderboardTabPanel.setVisible(false);
        liveRaceDiv.getStyle().setVisibility(Visibility.HIDDEN);
        
        leaderboardNavigation = placesNavigator.getRegattaAnalyticsNavigation(event.id.toString(), RegattaNavigationTabs.Leaderboard, leaderboardName, 
                event.getBaseURL(), event.isOnRemoteServer());
        competitorChartsNavigation = placesNavigator.getRegattaAnalyticsNavigation(event.id.toString(), RegattaNavigationTabs.CompetitorAnalytics, leaderboardName, 
                event.getBaseURL(), event.isOnRemoteServer());
        
        leaderboardAnchor.setHref(leaderboardNavigation.getTargetUrl());
        competitorChartsAnchor.setHref(competitorChartsNavigation.getTargetUrl());
    }
    
    public void createRegattaAnalyticsViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        this.autoRefreshTimer = timer;
        regattaAnalyticsManager = new RegattaAnalyticsDataManager(sailingService, asyncActionsExecutor, timer, errorReporter, userAgent);
        regattaAnalyticsManager.createLeaderboardPanel(leaderboardSettings, preselectedRace, leaderboardGroupName, 
                leaderboardName, showRaceDetails, autoExpandLastRaceColumn);

        List<DetailType> availableDetailsTypes = new ArrayList<DetailType>();
        DetailType initialDetailType = DetailType.REGATTA_RANK;
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_TOTAL_POINTS_SUM);
        regattaAnalyticsManager.createMultiCompetitorChart(leaderboardName, initialDetailType);

        oldLeaderboardComposite.setLeaderboard(regattaAnalyticsManager.getLeaderboardPanel(), autoRefreshTimer);
        regattaAnalyticsManager.getLeaderboardPanel().addLeaderboardUpdateListener(this);
        
        oldCompetitorChartsComposite.setChart(regattaAnalyticsManager.getMultiCompetitorChart(), availableDetailsTypes, initialDetailType);
        regattaAnalyticsManager.hideCompetitorChart();
        
        switch(navigationTab) {
            case Leaderboard:
                setActiveTabPanel(leaderboardTabPanel, leaderboardAnchor);
                break;
            case CompetitorAnalytics:
                setActiveTabPanel(competitorChartsTabPanel, competitorChartsAnchor);
                DetailType selectedChartDetailType = oldCompetitorChartsComposite.getSelectedChartDetailType();
                regattaAnalyticsManager.showCompetitorChart(selectedChartDetailType);
                break;
        }
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        if(activeTabPanel == leaderboardTabPanel) {
            StringMessages stringMessages = StringMessages.INSTANCE;
            
            if(leaderboard != null) {
                List<Pair<RaceColumnDTO, FleetDTO>> liveRaces = leaderboard.getLiveRaces(autoRefreshTimer.getLiveTimePointInMillis());
                boolean hasLiveRace = !liveRaces.isEmpty();
                if (hasLiveRace) {
                    String liveRaceText = "";
                    if(liveRaces.size() == 1) {
                        Pair<RaceColumnDTO, FleetDTO> liveRace = liveRaces.get(0);
                        liveRaceText = stringMessages.raceIsLive("'" + liveRace.getA().getRaceColumnName() + "'");
                    } else {
                        String raceNames = "";
                        for (Pair<RaceColumnDTO, FleetDTO> liveRace : liveRaces) {
                            raceNames += "'" + liveRace.getA().getRaceColumnName() + "', ";
                        }
                        // remove last ", "
                        raceNames = raceNames.substring(0, raceNames.length() - 2);
                        liveRaceText = stringMessages.racesAreLive(raceNames);
                    }
                    liveRaceDiv.setInnerText(liveRaceText);
                } else {
                    liveRaceDiv.setInnerText("");
                }
                liveRaceDiv.getStyle().setVisibility(hasLiveRace ? Visibility.VISIBLE : Visibility.HIDDEN);
                
                oldLeaderboardComposite.updatedLeaderboard(leaderboard, hasLiveRace);
            }
        }
    }

    @Override
    public void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn) {
    }
    
    @UiHandler("leaderboardAnchor")
    void leaderboardTabClicked(ClickEvent event) {
        setActiveTabPanel(leaderboardTabPanel, leaderboardAnchor);
        regattaAnalyticsManager.hideCompetitorChart();
        handleClickEvent(event, leaderboardNavigation);
    }
    
    @UiHandler("competitorChartsAnchor")
    void competitorChartsTabClicked(ClickEvent event) {
        setActiveTabPanel(competitorChartsTabPanel, competitorChartsAnchor);
        DetailType selectedChartDetailType = oldCompetitorChartsComposite.getSelectedChartDetailType();
        regattaAnalyticsManager.showCompetitorChart(selectedChartDetailType);
        oldCompetitorChartsComposite.updateSelectionState(regattaAnalyticsManager.getCompetitorSelectionProvider());
        handleClickEvent(event, competitorChartsNavigation);
    }

    private void setActiveTabPanel(HTMLPanel newActivePanel, Anchor newActiveAnchor) {
        if(activeTabPanel != null) {
            activeTabPanel.setVisible(false);
            activeAnchor.removeStyleName(SharedResources.INSTANCE.mainCss().navbar_buttonactive());
        }
        
        activeTabPanel = newActivePanel;
        activeAnchor = newActiveAnchor;
        activeTabPanel.setVisible(true);
        activeAnchor.addStyleName(SharedResources.INSTANCE.mainCss().navbar_buttonactive());
    }
    
    private void handleClickEvent(ClickEvent e, PlaceNavigation<?> placeNavigation) {
        if (HYPERLINK_IMPL.handleAsClick((Event) e.getNativeEvent())) {
            // don't use the placecontroller for navigation here as we want to avoid a page reload
            History.newItem(placeNavigation.getHistoryUrl(), false);
            e.preventDefault();
         }
    }
}

