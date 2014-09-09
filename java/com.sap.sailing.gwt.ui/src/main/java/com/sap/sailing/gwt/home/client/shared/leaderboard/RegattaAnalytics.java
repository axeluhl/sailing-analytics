package com.sap.sailing.gwt.home.client.shared.leaderboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.client.HomeResources;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.header.CompactEventHeader;
import com.sap.sailing.gwt.home.client.place.event.regattaleaderboard.EventRegattaLeaderboardResources;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class RegattaAnalytics extends Composite implements LeaderboardUpdateListener {
    private static RegattaAnalyticsUiBinder uiBinder = GWT.create(RegattaAnalyticsUiBinder.class);

    interface RegattaAnalyticsUiBinder extends UiBinder<Widget, RegattaAnalytics> {
    }

    @UiField HTMLPanel oldLeaderboardPanel;
    @UiField HTMLPanel competitorChartsPanel;
    @UiField(provided=true) CompactEventHeader eventHeader;
    
    // temp fields --> will be moved to a leaderboard partial later on
    @UiField SpanElement title;
    @UiField Anchor settingsAnchor;
    @UiField Anchor autoRefreshAnchor;
    @UiField ParagraphElement lastScoringUpdateTimeDiv;
    @UiField ParagraphElement lastScoringCommentDiv;
    @UiField ParagraphElement scoringSchemeDiv;
    @UiField DivElement liveRaceDiv;

    @UiField HTMLPanel leaderboardTabPanel;
    @UiField HTMLPanel competitorChartsTabPanel;
    private HTMLPanel activeTabPanel;
    private Anchor activeAnchor;
    
    @UiField Anchor leaderboardAnchor;
    @UiField Anchor competitorChartsAnchor;
    @UiField(provided=true) ListBox chartTypeSelectionListBox;
    
    private AbstractRegattaAnalyticsManager regattaAnalyticsManager;
    private Timer autoRefreshTimer;
    private final List<DetailType> availableDetailsTypes;
    
    public RegattaAnalytics(EventDTO event, String leaderboardName, Timer timerForClientServerOffset, PlaceNavigator placeNavigator) {
        eventHeader = new CompactEventHeader(event, leaderboardName, placeNavigator);
    
        availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_TOTAL_POINTS_SUM);
        DetailType initialDetailType = DetailType.REGATTA_RANK;
        
        chartTypeSelectionListBox = new ListBox(false);
        int i = 0;
        for (DetailType detailType : availableDetailsTypes) {
            chartTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialDetailType) {
                chartTypeSelectionListBox.setSelectedIndex(i);
            }
            i++;
        }
        
        EventRegattaLeaderboardResources.INSTANCE.css().ensureInjected();
        RegattaAnalyticsResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        title.setInnerText(leaderboardName);

        activeAnchor = leaderboardAnchor;
        activeTabPanel = leaderboardTabPanel;
        activeAnchor.addStyleName(HomeResources.INSTANCE.mainCss().navbar_buttonactive());

        competitorChartsTabPanel.setVisible(false);
    }
    
    public void createLeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        this.autoRefreshTimer = timer;
        regattaAnalyticsManager = new LeaderboardViewer(sailingService, asyncActionsExecutor, timer, errorReporter, userAgent);
        regattaAnalyticsManager.createLeaderboardPanel(leaderboardSettings, preselectedRace, leaderboardGroupName, leaderboardName, showRaceDetails, autoExpandLastRaceColumn);
        regattaAnalyticsManager.createMultiCompetitorChart(leaderboardName, DetailType.REGATTA_RANK);

        ScrollPanel contentScrollPanel = new ScrollPanel();
        contentScrollPanel.setWidget(regattaAnalyticsManager.getLeaderboardPanel());
        oldLeaderboardPanel.add(contentScrollPanel);
        regattaAnalyticsManager.getLeaderboardPanel().addLeaderboardUpdateListener(this);
        
        competitorChartsPanel.add(regattaAnalyticsManager.getMultiCompetitorChart());
        regattaAnalyticsManager.hideCompetitorChart();
    }

    public void createMetaLeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards) {
        this.autoRefreshTimer = timer;
        regattaAnalyticsManager = new MetaLeaderboardViewer(sailingService, asyncActionsExecutor, timer, errorReporter, userAgent);
        regattaAnalyticsManager.createLeaderboardPanel(leaderboardSettings, preselectedRace, leaderboardGroupName, metaLeaderboardName, showRaceDetails, autoExpandLastRaceColumn);
       
//        ScrollPanel contentScrollPanel = new ScrollPanel();
//        contentScrollPanel.setWidget(regattaAnalyticsManager.getLeaderboardPanel());
//        oldLeaderboardPanel.add(contentScrollPanel);
        oldLeaderboardPanel.add(regattaAnalyticsManager.getLeaderboardPanel());
        
        regattaAnalyticsManager.hideCompetitorChart();
    }
    
    @UiHandler("autoRefreshAnchor")
    void toogleAutoRefreshClicked(ClickEvent event) {
        if (autoRefreshTimer.getPlayState() == PlayStates.Playing) {
            autoRefreshTimer.pause();
            autoRefreshAnchor.getElement().getStyle().setBackgroundColor("#f0ab00");
        } else {
            // playing the standalone leaderboard means putting it into live mode
            autoRefreshTimer.setPlayMode(PlayModes.Live);
            autoRefreshAnchor.getElement().getStyle().setBackgroundColor("red");
        }
    }
    
    @UiHandler("settingsAnchor")
    void settingsClicked(ClickEvent event) {
        LeaderboardPanel leaderboardComponent = regattaAnalyticsManager.getLeaderboardPanel();
        
        final String componentName = leaderboardComponent.getLocalizedShortName();
        final String debugIdPrefix = DebugIdHelper.createDebugId(componentName);

        SettingsDialog<LeaderboardSettings> dialog = new SettingsDialog<LeaderboardSettings>(leaderboardComponent,
                StringMessages.INSTANCE);
        dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
        dialog.show();
    }

    @Override
    public void updatedLeaderboard(LeaderboardDTO leaderboard) {
        if(activeTabPanel == leaderboardTabPanel) {
            StringMessages stringMessages = StringMessages.INSTANCE;
            
            if(leaderboard != null) {
                lastScoringCommentDiv.setInnerText(leaderboard.getComment() != null ? leaderboard.getComment() : "");
                scoringSchemeDiv.setInnerText(leaderboard.scoringScheme != null ? ScoringSchemeTypeFormatter.format(leaderboard.scoringScheme, StringMessages.INSTANCE) : "");
                if (leaderboard.getTimePointOfLastCorrectionsValidity() != null) {
                    Date lastCorrectionDate = leaderboard.getTimePointOfLastCorrectionsValidity();
                    String lastUpdate = DateAndTimeFormatterUtil.defaultDateFormatter.render(lastCorrectionDate) + ", "
                            + DateAndTimeFormatterUtil.longTimeFormatter.render(lastCorrectionDate);
                    lastScoringUpdateTimeDiv.setInnerText(lastUpdate);
                } else {
                    lastScoringUpdateTimeDiv.setInnerText("");
                }
                
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
                lastScoringUpdateTimeDiv.getStyle().setVisibility(!hasLiveRace ? Visibility.VISIBLE : Visibility.HIDDEN);
                liveRaceDiv.getStyle().setVisibility(hasLiveRace ? Visibility.VISIBLE : Visibility.HIDDEN);
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
    }
    
    @UiHandler("competitorChartsAnchor")
    void competitorChartsTabClicked(ClickEvent event) {
        setActiveTabPanel(competitorChartsTabPanel, competitorChartsAnchor);
        DetailType selectedChartDetailType = getSelectedChartDetailType();
        regattaAnalyticsManager.showCompetitorChart(selectedChartDetailType);
    }

    @UiHandler("chartTypeSelectionListBox")
    void chartTypeSelectionChanged(ChangeEvent event) {
        DetailType selectedChartDetailType = getSelectedChartDetailType();
        regattaAnalyticsManager.showCompetitorChart(selectedChartDetailType);
    }
    
    private DetailType getSelectedChartDetailType() {
        DetailType result = null;
        int selectedIndex = chartTypeSelectionListBox.getSelectedIndex();
        String selectedDetailType = chartTypeSelectionListBox.getValue(selectedIndex);
        for (DetailType detailType : availableDetailsTypes) {
            if (detailType.name().equals(selectedDetailType)) {
                result = detailType;
                break;
            }
        }
        return result;
    }

    private void setActiveTabPanel(HTMLPanel newActivePanel, Anchor newActiveAnchor) {
        if(activeTabPanel != null) {
            activeTabPanel.setVisible(false);
            activeAnchor.removeStyleName(HomeResources.INSTANCE.mainCss().navbar_buttonactive());
        }
        
        activeTabPanel = newActivePanel;
        activeAnchor = newActiveAnchor;
        activeTabPanel.setVisible(true);
        activeAnchor.addStyleName(HomeResources.INSTANCE.mainCss().navbar_buttonactive());
    }
}
