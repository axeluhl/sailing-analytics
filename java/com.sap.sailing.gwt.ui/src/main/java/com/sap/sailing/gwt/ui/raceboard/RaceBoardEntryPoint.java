
package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class RaceBoardEntryPoint extends AbstractSailingEntryPoint {
    private RaceDTO selectedRace;

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

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);

    @Override
    protected void doOnModuleLoad() {    
        super.doOnModuleLoad();
     
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
        boolean showViewSimulation = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_SIMULATION, true /* default*/);
        String activeCompetitorsFilterSetName = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_VIEW_COMPETITOR_FILTER, null /* default*/);
        final boolean canReplayWhileLiveIsPossible = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_CAN_REPLAY_DURING_LIVE_RACES, false);
        final boolean autoSelectMedia = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_AUTOSELECT_MEDIA, false);
        final String defaultMedia = GwtHttpRequestUtils.getStringParameter(RaceBoardViewConfiguration.PARAM_DEFAULT_MEDIA, null /* default*/);
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, true /* default*/);
        raceboardViewConfig = new RaceBoardViewConfiguration(activeCompetitorsFilterSetName, showLeaderboard,
                showWindChart, showCompetitorsChart, showViewStreamlets, showViewSimulation, canReplayWhileLiveIsPossible, autoSelectMedia, defaultMedia);

        final ParallelExecutionCallback<List<String>> getLeaderboardNamesCallback = new ParallelExecutionCallback<List<String>>();
        final ParallelExecutionCallback<List<RegattaDTO>> getRegattasCallback = new ParallelExecutionCallback<List<RegattaDTO>>();
        final ParallelExecutionCallback<LeaderboardGroupDTO> getLeaderboardGroupByNameCallback = new ParallelExecutionCallback<LeaderboardGroupDTO>();
        final ParallelExecutionCallback<EventDTO> getEventByIdCallback = new ParallelExecutionCallback<EventDTO>();
        List<ParallelExecutionCallback<?>> callbacks = new ArrayList<>();
        callbacks.add(getLeaderboardNamesCallback);
        callbacks.add(getRegattasCallback);
        if (leaderboardGroupName != null) {
            callbacks.add(getLeaderboardGroupByNameCallback);
        }
        if (eventId != null) {
            callbacks.add(getEventByIdCallback);
        }
        ParallelExecutionCallback<?>[] callbackArray = callbacks.toArray(new ParallelExecutionCallback<?>[0]);
        new ParallelExecutionHolder(callbackArray) {
            @Override
            public void handleSuccess() {
                checkUrlParameters(getLeaderboardNamesCallback.getData(),
                        leaderboardGroupName == null ? null : getLeaderboardGroupByNameCallback.getData(),
                        canReplayWhileLiveIsPossible, getRegattasCallback.getData(), eventId == null ? null : getEventByIdCallback.getData(),
                        showMapControls);
            }

            @Override
            public void handleFailure(Throwable t) {
                reportError("Error trying to create the raceboard: " + t.getMessage());
            }
        };
        sailingService.getRegattas(getRegattasCallback);
        sailingService.getLeaderboardNames(getLeaderboardNamesCallback);
        if (eventId != null) {
            sailingService.getEventById(eventId, /* withStatisticalData */ false, getEventByIdCallback);
        }
        if (leaderboardGroupName != null) {
            sailingService.getLeaderboardGroupByName(leaderboardGroupNameParamValue, false /*withGeoLocationData*/, getLeaderboardGroupByNameCallback);
        }
    }
    
    private void createErrorPage(String message) {
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages(), this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RootPanel.get().add(logoAndTitlePanel);
        RootPanel.get().add(new Label(message));
    }

    private void checkUrlParameters(List<String> leaderboardNames, LeaderboardGroupDTO leaderboardGroup,
            boolean canReplayWhileLiveIsPossible, List<RegattaDTO> regattas, EventDTO event,
            boolean showMapControls) {
        if (!leaderboardNames.contains(leaderboardName)) {
            createErrorPage(getStringMessages().noSuchLeaderboard());
            return;
        }
        if (eventId != null && event == null) {
            createErrorPage(getStringMessages().noSuchEvent());
        }
        if (leaderboardGroupName != null && leaderboardGroup != null) {
            boolean foundLeaderboard = false;
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.leaderboards) {
                if (leaderboard.name.equals(leaderboardName)) {
                    foundLeaderboard = true;
                    break;
                }
            }
            if (!foundLeaderboard) {
                createErrorPage(getStringMessages().leaderboardNotContainedInLeaderboardGroup(leaderboardName, leaderboardGroupName));
                return;
            }
            if (event != null) {
                boolean foundLeaderboardGroupInEvent = false;
                for (LeaderboardGroupDTO lg : event.getLeaderboardGroups()) {
                    if (lg.getId().equals(leaderboardGroup.getId())) {
                        foundLeaderboardGroupInEvent = true;
                        break;
                    }
                }
                if (!foundLeaderboardGroupInEvent) {
                    createErrorPage(getStringMessages().leaderboardGroupNotContainedInEvent(leaderboardGroupName, event.getName()));
                }
            }
        }
        RaceDTO race = findRace(regattaName, raceName, regattas);
        selectedRace = race;
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
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, mediaService, getUserService(), asyncActionsExecutor, timer, raceSelectionModel,
                leaderboardName, leaderboardGroupName, event, raceboardViewConfig, RaceBoardEntryPoint.this, getStringMessages(), userAgent, raceTimesInfoProvider, showMapControls);
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
