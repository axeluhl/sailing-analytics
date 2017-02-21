
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.common.client.formfactor.DeviceDetector;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.DefaultOnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class RaceBoardEntryPoint extends AbstractSailingEntryPoint {
    private RaceWithCompetitorsDTO selectedRace;

    private static final String PARAM_REGATTA_NAME = "regattaName";
    private static final String PARAM_RACE_NAME = "raceName";
    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";
    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    private static final String PARAM_EVENT_ID = "eventId";
    
    /**
     * Controls the predefined mode into which to switch or configure the race viewer. 
     */
    private static final String PARAM_MODE = "mode";
    
    private String regattaName;
    private String raceName;
    private String leaderboardName;
    private String leaderboardGroupName;
    private UUID eventId;

    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);
        // read mandatory parameters
        regattaName = Window.Location.getParameter(PARAM_REGATTA_NAME);
        raceName = Window.Location.getParameter(PARAM_RACE_NAME);
        final String modeName = Window.Location.getParameter(PARAM_MODE);
        RaceBoardMode mode;
        RaceBoardModes modeType = null;
        if (modeName == null) {
            mode = null;
        } else {
            try {
                modeType = RaceBoardModes.valueOf(modeName);
                mode = modeType.getMode();
            } catch (IllegalArgumentException e) {
                GWT.log("Couldn't resolve RaceBoard mode " + modeName);
                mode = null;
                modeType = null;
            }
        }
        final RaceBoardMode finalMode = mode;
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
        
        final boolean showChartMarkEditMediaButtonsAndVideo = !DeviceDetector.isMobile();
        
        String componentContextGlobalDefinition = "RaceBoardEntryPoint";
        if(modeType != null) {
            componentContextGlobalDefinition += "." + modeType.toString();
        }
        RaceBoardPerspectiveLifecycle lifeCycle = new RaceBoardPerspectiveLifecycle(null, StringMessages.INSTANCE);
        ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>> context = new ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>>(
                lifeCycle,
                new UserSettingsStorageManager<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>>(
                        getUserService(), componentContextGlobalDefinition + "." + lifeCycle.getComponentId(),
                        UserSettingsStorageManager.buildContextDefinitionId(regattaName, raceName, leaderboardName,
                                leaderboardGroupName,
                eventId == null ? null : eventId.toString())));
        
        context.initInitialSettings();
        AsyncCallback<RaceboardDataDTO> asyncCallback = new AsyncCallback<RaceboardDataDTO>() {
            @Override
            public void onSuccess(RaceboardDataDTO raceboardData) {
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
                
                context.initInitialSettings(new DefaultOnSettingsLoadedCallback<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>>() {
                    @Override
                    public void onSuccess(PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> initialSettings) {
                        final RaceBoardPanel raceBoardPanel = createPerspectivePage(null, context, initialSettings,
                                        raceboardData, showChartMarkEditMediaButtonsAndVideo, lifeCycle);
                        if (finalMode != null) {
                            finalMode.applyTo(raceBoardPanel);
                        }
                    }
                    });
            }
            
            @Override
            public void onFailure(Throwable caught) {
                reportError("Error trying to create the raceboard: " + caught.getMessage());
            }
        };
        
        sailingService.getRaceboardData(regattaName, raceName, leaderboardName, leaderboardGroupName, eventId, asyncCallback);
    }
    
    private void createErrorPage(String message) {
        final DockLayoutPanel vp = new DockLayoutPanel(Unit.PX);
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages(), this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RootLayoutPanel.get().add(vp);
        vp.addNorth(logoAndTitlePanel, 100);
        vp.add(new Label(message));
    }

    private RaceBoardPanel createPerspectivePage(Component<?> parent,
            ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveSettings>> context,
            PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> settings, RaceboardDataDTO raceboardData,
            boolean showChartMarkEditMediaButtonsAndVideo, RaceBoardPerspectiveLifecycle raceLifeCycle) {
        selectedRace = raceboardData.getRace();
        Window.setTitle(selectedRace.getName());
        Timer timer = new Timer(PlayModes.Replay, 1000l);
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(sailingService, asyncActionsExecutor, this,
                Collections.singletonList(selectedRace.getRaceIdentifier()), 5000l /* requestInterval*/);
  
        RaceBoardPanel raceBoardPerspective = new RaceBoardPanel(parent, context,
                raceLifeCycle,
                settings,
                sailingService, mediaService, getUserService(), asyncActionsExecutor,
                raceboardData.getCompetitorAndTheirBoats(), timer, selectedRace.getRaceIdentifier(), leaderboardName,
                leaderboardGroupName, eventId, RaceBoardEntryPoint.this, getStringMessages(), userAgent,
                raceTimesInfoProvider, showChartMarkEditMediaButtonsAndVideo);
        RootLayoutPanel.get().add(raceBoardPerspective.getEntryWidget());
        return raceBoardPerspective;
    }  
}
