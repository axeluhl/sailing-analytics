
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class RaceBoardEntryPoint extends AbstractSailingEntryPoint {
    private RaceWithCompetitorsDTO selectedRace;

    private static final String PARAM_REGATTA_NAME = "regattaName";
    private static final String PARAM_RACE_NAME = "raceName";
    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";
    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    private static final String PARAM_EVENT_ID = "eventId";
    
    private String regattaName;
    private String raceName;
    private String leaderboardName;
    private String leaderboardGroupName;
    private UUID eventId;
    private RaceBoardViewConfiguration raceboardViewConfig;

    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);
        // read mandatory parameters
        regattaName = Window.Location.getParameter(PARAM_REGATTA_NAME);
        raceName = Window.Location.getParameter(PARAM_RACE_NAME);
        String leaderboardNameParamValue = Window.Location.getParameter(PARAM_LEADERBOARD_NAME);
        String leaderboardGroupNameParamValue = Window.Location.getParameter(PARAM_LEADERBOARD_GROUP_NAME);
        if (leaderboardNameParamValue != null && !leaderboardNameParamValue.isEmpty()) {
            leaderboardName = leaderboardNameParamValue;
        }
        if (leaderboardGroupNameParamValue != null && !leaderboardGroupNameParamValue.isEmpty()) {
            leaderboardGroupName = leaderboardGroupNameParamValue; 
        }
        String eventIdParamValue = Window.Location.getParameter(PARAM_EVENT_ID);
        if (eventIdParamValue != null && !eventIdParamValue.isEmpty()) {
            eventId = UUID.fromString(eventIdParamValue);
        }
        if (leaderboardGroupNameParamValue != null && !leaderboardGroupNameParamValue.isEmpty()) {
            leaderboardGroupName = leaderboardGroupNameParamValue; 
        }
        if (regattaName == null || regattaName.isEmpty() || raceName == null || raceName.isEmpty() ||
                leaderboardName == null || leaderboardName.isEmpty()) {
            createErrorPage("This page requires a valid regatta name, race name and leaderboard name.");
            return;
        }
        
        // read optional parameters 
        boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_LEADERBOARD, true /* default*/);
        boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_WINDCHART, false /* default*/);
        boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_COMPETITORSCHART, false /* default*/);
        boolean showViewStreamlets = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_STREAMLETS, false /* default*/);
        boolean showViewStreamletColors = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_STREAMLET_COLORS, false /* default*/);        
        boolean showViewSimulation = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_SIMULATION, true /* default*/);
        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_VIEW_COMPETITOR_FILTER, null /* default*/);
        final boolean canReplayWhileLiveIsPossible = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, false);
        final boolean autoSelectMedia = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_AUTOSELECT_MEDIA, true);
        final String defaultMedia = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_DEFAULT_MEDIA, null /* default*/);
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, true /* default*/);
        raceboardViewConfig = new RaceBoardViewConfiguration(activeCompetitorsFilterSetName, showLeaderboard,
                showWindChart, showCompetitorsChart, showViewStreamlets, showViewStreamletColors, showViewSimulation, canReplayWhileLiveIsPossible, autoSelectMedia, defaultMedia);

        sailingService.getRaceboardData(regattaName, raceName, leaderboardName, leaderboardGroupName, eventId, new AsyncCallback<RaceboardDataDTO>() {
            @Override
            public void onSuccess(RaceboardDataDTO result) {
                // Determine if the screen is large enough to display charts such as the competitor chart or the wind chart.
                // This decision is made once based on the initial screen height. Resizing the window afterwards will have
                // no impact on the chart support, i.e. they are available/unavailable based on the initial decision.
                boolean isScreenLargeEnoughToOfferChartSupport = Document.get().getClientHeight() >= 600;
                checkUrlParameters(result, canReplayWhileLiveIsPossible, showMapControls,
                        isScreenLargeEnoughToOfferChartSupport);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                reportError("Error trying to create the raceboard: " + caught.getMessage());
            }
        });
    }
    
    private void createErrorPage(String message) {
        final DockLayoutPanel vp = new DockLayoutPanel(Unit.PX);
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages(), this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RootLayoutPanel.get().add(vp);
        vp.addNorth(logoAndTitlePanel, 100);
        vp.add(new Label(message));
    }

    private void checkUrlParameters(RaceboardDataDTO raceboardData, boolean canReplayWhileLiveIsPossible, boolean showMapControls,
            boolean isScreenLargeEnoughToOfferChartSupport) {
        if (!raceboardData.isValidLeaderboard()) {
            createErrorPage(getStringMessages().noSuchLeaderboard());
            return;
        }
        if (eventId != null && !raceboardData.isValidEvent()) {
            createErrorPage(getStringMessages().noSuchEvent());
        }
        if (leaderboardGroupName != null) {
            if(!raceboardData.isValidLeaderboardGroup()) {
                createErrorPage(getStringMessages().leaderboardNotContainedInLeaderboardGroup(leaderboardName, leaderboardGroupName));
                return;
            }
            if (eventId != null && raceboardData.isValidLeaderboardGroup() && !raceboardData.isValidEvent()) {
                createErrorPage(getStringMessages().leaderboardGroupNotContainedInEvent(leaderboardGroupName, eventId.toString()));
                return;
            }
        }
        if (raceboardData.getRace() == null) {
            createErrorPage("Could not obtain a race with name " + raceName + " for a regatta with name " + regattaName);
            return;
        }
        selectedRace = raceboardData.getRace();
        Window.setTitle(selectedRace.getName());
        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RegattaAndRaceIdentifier> singletonList = Collections.singletonList(selectedRace.getRaceIdentifier());
        raceSelectionModel.setSelection(singletonList);
        Timer timer = new Timer(PlayModes.Replay, 1000l);
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, this, singletonList, 5000l /* requestInterval*/);
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, mediaService, getUserService(), asyncActionsExecutor,
                raceboardData.getCompetitorAndTheirBoats(), timer, raceSelectionModel, leaderboardName, leaderboardGroupName, eventId, 
                raceboardViewConfig, RaceBoardEntryPoint.this, getStringMessages(), userAgent, raceTimesInfoProvider, showMapControls,
                isScreenLargeEnoughToOfferChartSupport);

        createRaceBoardInOneScreenMode(raceBoardPanel, raceboardViewConfig);
    }  

    private FlowPanel createTimePanel(RaceBoardPanel raceBoardPanel) {
        FlowPanel timeLineInnerBgPanel = new ResizableFlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimePanel());
        
        FlowPanel timeLineInnerPanel = new ResizableFlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new ResizableFlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
    }

    private void createRaceBoardInOneScreenMode(final RaceBoardPanel raceBoardPanel,
            RaceBoardViewConfiguration raceboardViewConfiguration) {
        final DockLayoutPanel p = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(p);
        final FlowPanel timePanel = createTimePanel(raceBoardPanel);
        final Button toggleButton = raceBoardPanel.getTimePanel().getAdvancedToggleButton();
        toggleButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean advancedModeShown = raceBoardPanel.getTimePanel().toggleAdvancedMode();
                if (advancedModeShown) {
                    p.setWidgetSize(timePanel, 96);
                    toggleButton.removeStyleDependentName("Closed");
                    toggleButton.addStyleDependentName("Open");
                } else {
                    p.setWidgetSize(timePanel, 67);
                    toggleButton.addStyleDependentName("Closed");
                    toggleButton.removeStyleDependentName("Open");
                }
            }
        });
        p.addSouth(timePanel, 67);
        p.add(raceBoardPanel);
        p.addStyleName("dockLayoutPanel");
    }
}
