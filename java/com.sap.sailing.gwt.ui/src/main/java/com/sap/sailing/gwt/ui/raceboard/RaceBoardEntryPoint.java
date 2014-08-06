
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.UserManagementService;
import com.sap.sailing.gwt.ui.client.UserManagementServiceAsync;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.useragent.UserAgentChecker;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    private RaceDTO selectedRace;

    private static final String PARAM_REGATTA_NAME = "regattaName";
    private static final String PARAM_RACE_NAME = "raceName";
    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";
    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    
    private String regattaName;
    private String raceName;
    private String leaderboardName;
    private String leaderboardGroupName;
    private RaceBoardViewConfiguration raceboardViewConfig;

    private GlobalNavigationPanel globalNavigationPanel;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    private boolean toolbarAndLogoAndTitleBarHidden;

    @Override
    protected void doOnModuleLoad() {    
        super.doOnModuleLoad();
     
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, RemoteServiceMappingConstants.userManagementServiceRemotePath);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);
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
        if (regattaName == null || regattaName.isEmpty() || raceName == null || raceName.isEmpty() ||
                leaderboardName == null || leaderboardName.isEmpty()) {
            createErrorPage("This page requires a valid regatta name, race name and leaderboard name.");
            return;
        }
        
        // read optional parameters 
        boolean showLeaderboard = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_LEADERBOARD, false /* default*/);
        boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_WINDCHART, false /* default*/);
        boolean showCompetitorsChart = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_COMPETITORSCHART, false /* default*/);
        boolean showViewStreamlets = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_STREAMLETS, false /* default*/);
        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_VIEW_COMPETITOR_FILTER, null /* default*/);
        final boolean canReplayWhileLiveIsPossible = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, false);
        final boolean autoSelectMedia = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_AUTOSELECT_MEDIA, false);
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, true /* default*/);
        final boolean showNavigationPanel = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_NAVIGATION_PANEL, true /* default */);
        raceboardViewConfig = new RaceBoardViewConfiguration(activeCompetitorsFilterSetName, showLeaderboard,
                showWindChart, showCompetitorsChart, showViewStreamlets, canReplayWhileLiveIsPossible, autoSelectMedia,
                showNavigationPanel);

        final ParallelExecutionCallback<List<String>> getLeaderboardNamesCallback = new ParallelExecutionCallback<List<String>>();  
        final ParallelExecutionCallback<List<RegattaDTO>> getRegattasCallback = new ParallelExecutionCallback<List<RegattaDTO>>();  
        final ParallelExecutionCallback<LeaderboardGroupDTO> getLeaderboardGroupByNameCallback = new ParallelExecutionCallback<LeaderboardGroupDTO>();  
        final ParallelExecutionCallback<UserDTO> getUserCallback = new ParallelExecutionCallback<UserDTO>();  
        if (leaderboardGroupName != null) {
            new ParallelExecutionHolder(getLeaderboardNamesCallback, getLeaderboardGroupByNameCallback, getRegattasCallback, getUserCallback) {
                @Override
                public void handleSuccess() {
                    checkUrlParameters(getLeaderboardNamesCallback.getData(),
                            getLeaderboardGroupByNameCallback.getData(), canReplayWhileLiveIsPossible, getRegattasCallback.getData(), getUserCallback.getData(), showMapControls);
                }
                @Override
                public void handleFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            };
        } else {
            new ParallelExecutionHolder(getLeaderboardNamesCallback, getRegattasCallback, getUserCallback) {
                @Override
                public void handleSuccess() {
                    checkUrlParameters(getLeaderboardNamesCallback.getData(), null, canReplayWhileLiveIsPossible, getRegattasCallback.getData(), getUserCallback.getData(), showMapControls);
                }
                @Override
                public void handleFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            };
        }
        sailingService.getRegattas(getRegattasCallback);
        sailingService.getLeaderboardNames(getLeaderboardNamesCallback);
        if (leaderboardGroupName != null) {
            sailingService.getLeaderboardGroupByName(leaderboardGroupNameParamValue, false /*withGeoLocationData*/, getLeaderboardGroupByNameCallback);
        }
        userManagementService.getUser(getUserCallback);
    }

    private void checkUrlParameters(List<String> leaderboardNames, LeaderboardGroupDTO leaderboardGroup, boolean canReplayWhileLiveIsPossible, List<RegattaDTO> regattas, UserDTO user, boolean showMapControls) {
        if (!leaderboardNames.contains(leaderboardName)) {
          createErrorPage(stringMessages.noSuchLeaderboard());
          return;
        }
        if (leaderboardGroupName != null && leaderboardGroup != null) {
            boolean foundLeaderboard = false; 
            for(StrippedLeaderboardDTO leaderBoard:  leaderboardGroup.leaderboards) {
                if(leaderBoard.name.equals(leaderboardName)) {
                    foundLeaderboard = true;
                    break;
                }
            }
            if (!foundLeaderboard) {
                createErrorPage("the leaderboard is not contained in this leaderboard group.");
                return;
            }
        }
        selectedRace = findRace(regattaName, raceName, regattas);
        if (selectedRace == null) {
            createErrorPage("Could not obtain a race with name " + raceName + " for a regatta with name " + regattaName);
            return;
        }
        
        Window.setTitle(selectedRace.getName());

        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RegattaAndRaceIdentifier> singletonList = Collections.singletonList(selectedRace.getRaceIdentifier());
        raceSelectionModel.setSelection(singletonList);
        Timer timer = new Timer(PlayModes.Replay, 1000l);
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, this, singletonList, 5000l /* requestInterval*/);
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, mediaService, asyncActionsExecutor, user, timer, raceSelectionModel, leaderboardName,
                leaderboardGroupName, raceboardViewConfig, RaceBoardEntryPoint.this, stringMessages, userAgent, raceTimesInfoProvider, showMapControls);
        raceBoardPanel.fillRegattas(regattas);
        createRaceBoardInOneScreenMode(raceBoardPanel, raceboardViewConfig);
    }  

    private RaceDTO findRace(String regattaName, String raceName, List<RegattaDTO> regattas) {
        for (RegattaDTO regattaDTO : regattas) {
            if (regattaDTO.getName().equals(regattaName)) {
                for (RaceDTO raceDTO : regattaDTO.races) {
                    if (raceDTO.getName().equals(raceName)) {
                        return raceDTO;
                    }
                }
            }
        }
        return null;
    }

    private FlowPanel createTimePanel(RaceBoardPanel raceBoardPanel) {
        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimePanel());
        
        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
    }

    private FlowPanel createLogoAndTitlePanel(RaceBoardPanel raceBoardPanel) {
        globalNavigationPanel = new GlobalNavigationPanel(stringMessages, true, leaderboardName, leaderboardGroupName);
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(regattaName, selectedRace.getName(), stringMessages, this) {
            @Override
            public void onResize() {
                super.onResize();
                if (isSmallWidth()) {
                    remove(globalNavigationPanel);
                } else {
                    add(globalNavigationPanel);
                }
            }
        };
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        if (!isSmallWidth()) {
            logoAndTitlePanel.add(globalNavigationPanel);
        }
        return logoAndTitlePanel;
    }
    
    private void createRaceBoardInOneScreenMode(RaceBoardPanel raceBoardPanel,
            RaceBoardViewConfiguration raceboardViewConfiguration) {
        final DockLayoutPanel p = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(p);
        final Panel toolbarPanel = raceBoardPanel.getToolbarPanel();
        if (!UserAgentChecker.INSTANCE.isUserAgentSupported(userAgent)) {
            HTML lbl = new HTML(stringMessages.warningBrowserUnsupported());
            lbl.setStyleName("browserOptimizedMessage");
            toolbarPanel.add(lbl);
        }
        final FlowPanel logoAndTitlePanel = createLogoAndTitlePanel(raceBoardPanel);
        FlowPanel timePanel = createTimePanel(raceBoardPanel);
        p.addNorth(logoAndTitlePanel, 68);
        p.addNorth(toolbarPanel, 40);
        toolbarAndLogoAndTitleBarHidden = false;
        if (!raceboardViewConfiguration.isShowNavigationPanel()) {
            p.setWidgetHidden(toolbarPanel, true);
            globalNavigationPanel.setVisible(false);
            toolbarAndLogoAndTitleBarHidden = true;
        }

        p.addSouth(timePanel, 90);
        p.add(raceBoardPanel);
        p.addStyleName("dockLayoutPanel");

        addModeratorShortkeyFunctionality(p, toolbarPanel, raceBoardPanel, timePanel);
    }

    private void addModeratorShortkeyFunctionality(final DockLayoutPanel p, final Panel toolbarPanel,
            final RaceBoardPanel raceBoardPanel, final Panel timeWrapperPanel) {
        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                NativeEvent ne = event.getNativeEvent();
                if ("keydown".equals(ne.getType()) && ne.getCtrlKey()
                        && (ne.getKeyCode() == 'm' || ne.getKeyCode() == 'M')) {
                    ne.preventDefault();
                    Scheduler.get().scheduleDeferred(new Command() {
                        @Override
                        public void execute() {
                            p.setWidgetHidden(toolbarPanel, !toolbarAndLogoAndTitleBarHidden);
                            globalNavigationPanel.setVisible(toolbarAndLogoAndTitleBarHidden);
                            toolbarAndLogoAndTitleBarHidden = !toolbarAndLogoAndTitleBarHidden;
                            RaceTimePanel timePanel = raceBoardPanel.getTimePanel();
                            if (toolbarAndLogoAndTitleBarHidden) {
                                timePanel.hideControlsPanelAndMovePlayButtonUp();
                                p.setWidgetSize(timeWrapperPanel, 53);
                            } else {
                                timePanel.showControlsPanelAndMovePlayButtonDown();
                                p.setWidgetSize(timeWrapperPanel, 90);
                            }
                        }
                    });

                }
            }
        });
    }
}
