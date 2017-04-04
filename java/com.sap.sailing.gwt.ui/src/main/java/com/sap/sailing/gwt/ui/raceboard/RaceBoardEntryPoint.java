
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.common.client.formfactor.DeviceDetector;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.utils.StorageDefinitionIdFactory;
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
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.settings.StorageDefinitionId;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class RaceBoardEntryPoint extends AbstractSailingEntryPoint {
    private RaceWithCompetitorsDTO selectedRace;

    /**
     * Controls the predefined mode into which to switch or configure the race viewer. 
     */
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);

    private RaceboardContextDefinition raceboardContextDefinition;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService, RemoteServiceMappingConstants.mediaServiceRemotePath);

        raceboardContextDefinition = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new RaceboardContextDefinition());

        final RaceBoardModes finalMode;
        if (raceboardContextDefinition.getMode() != null) {
            finalMode = RaceBoardModes.valueOf(raceboardContextDefinition.getMode());
        } else {
            finalMode = null;
        }

        if (raceboardContextDefinition.getRegattaName() == null || raceboardContextDefinition.getRegattaName().isEmpty()
                || raceboardContextDefinition.getRaceName() == null || raceboardContextDefinition.getRaceName().isEmpty()
                || raceboardContextDefinition.getLeaderboardName() == null
                || raceboardContextDefinition.getLeaderboardName().isEmpty()) {
            createErrorPage("This page requires a valid regatta name, race name and leaderboard name.");
            return;
        }
        
        final boolean showChartMarkEditMediaButtonsAndVideo = !DeviceDetector.isMobile();
        
        final StorageDefinitionId storageDefinitionId = StorageDefinitionIdFactory.createStorageDefinitionIdForRaceBoard(raceboardContextDefinition);
        final RaceBoardPerspectiveLifecycle lifeCycle = new RaceBoardPerspectiveLifecycle(null, StringMessages.INSTANCE);
        ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> context = new ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>>(
                lifeCycle,
                new UserSettingsStorageManager<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>>(
                        getUserService(), storageDefinitionId)) {
            @Override
            public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
                // TODO bug3529 temporarily deactivated for raceboard due to finishing settings storage for race modes
                return false;
            };
        };
        
        context.initInitialSettings();
        AsyncCallback<RaceboardDataDTO> asyncCallback = new AsyncCallback<RaceboardDataDTO>() {
            @Override
            public void onSuccess(RaceboardDataDTO raceboardData) {
                if (!raceboardData.isValidLeaderboard()) {
                    createErrorPage(getStringMessages().noSuchLeaderboard());
                    return;
                }
                if (raceboardContextDefinition.getEventId() != null && !raceboardData.isValidEvent()) {
                    createErrorPage(getStringMessages().noSuchEvent());
                }
                if (raceboardContextDefinition.getLeaderboardGroupName() != null) {
                    if (!raceboardData.isValidLeaderboardGroup()) {
                        createErrorPage(getStringMessages().leaderboardNotContainedInLeaderboardGroup(
                                raceboardContextDefinition.getLeaderboardName(),
                                raceboardContextDefinition.getLeaderboardGroupName()));
                        return;
                    }
                    if (raceboardContextDefinition.getEventId() != null && raceboardData.isValidLeaderboardGroup()
                            && !raceboardData.isValidEvent()) {
                        createErrorPage(getStringMessages().leaderboardGroupNotContainedInEvent(
                                raceboardContextDefinition.getLeaderboardGroupName(),
                                raceboardContextDefinition.getEventId().toString()));
                        return;
                    }
                }
                if (raceboardData.getRace() == null) {
                    createErrorPage("Could not obtain a race with name " + raceboardContextDefinition.getRaceName()
                            + " for a regatta with name " + raceboardContextDefinition.getRegattaName());
                    return;
                }
                
                context.initInitialSettings(new DefaultOnSettingsLoadedCallback<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>>() {
                    @Override
                    public void onSuccess(PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> initialSettings) {
                        final RaceBoardPanel raceBoardPanel = createPerspectivePage(null, context, initialSettings,
                                        raceboardData, showChartMarkEditMediaButtonsAndVideo, lifeCycle);
                                if (finalMode != null) {
                                    finalMode.getMode().applyTo(raceBoardPanel);
                        }
                    }
                    });
            }
            
            @Override
            public void onFailure(Throwable caught) {
                reportError("Error trying to create the raceboard: " + caught.getMessage());
            }
        };
        
        sailingService.getRaceboardData(raceboardContextDefinition.getRegattaName(),
                raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getLeaderboardName(),
                raceboardContextDefinition.getLeaderboardGroupName(), raceboardContextDefinition.getEventId(),
                asyncCallback);
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
            ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> context,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings, RaceboardDataDTO raceboardData,
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
                raceboardData.getCompetitorAndTheirBoats(), timer, selectedRace.getRaceIdentifier(),
                raceboardContextDefinition.getLeaderboardName(), raceboardContextDefinition.getLeaderboardGroupName(),
                raceboardContextDefinition.getEventId(), RaceBoardEntryPoint.this, getStringMessages(), userAgent,
                raceTimesInfoProvider, showChartMarkEditMediaButtonsAndVideo);
        RootLayoutPanel.get().add(raceBoardPerspective.getEntryWidget());
        return raceBoardPerspective;
    }  
}
