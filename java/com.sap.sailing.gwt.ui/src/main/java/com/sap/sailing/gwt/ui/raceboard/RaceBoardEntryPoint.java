
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.utils.StoredSettingsLocationFactory;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.DefaultOnSettingsLoadedCallback;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorage;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

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
            createErrorPage(getStringMessages().requiresRegattaRaceAndLeaderboard());
            return;
        }
        
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
                    createErrorPage(getStringMessages().couldNotFindRaceInRegatta(raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getRegattaName()));
                    return;
                }
                
                final boolean showChartMarkEditMediaButtonsAndVideo = !DeviceDetector.isMobile();
                final StoredSettingsLocation storageDefinition = StoredSettingsLocationFactory
                        .createStoredSettingsLocatorForRaceBoard(raceboardContextDefinition,
                                finalMode != null ? finalMode.name() : null);
                sailingService.determineDetailTypesForCompetitorChart(raceboardContextDefinition.getLeaderboardGroupName(),
                        raceboardData.getRace().getRaceIdentifier(), new AsyncCallback<List<DetailType>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                reportError("Error trying to create the raceboard: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(List<DetailType> result) {
                                sailingService.getAvailableDetailTypesForLeaderboard(
                                        raceboardContextDefinition.getLeaderboardName(),
                                        new AsyncCallback<Collection<DetailType>>() {

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                reportError(
                                                        "Error trying to create the raceboard: " + caught.getMessage());
                                            }

                                            @Override
                                            public void onSuccess(Collection<DetailType> availableDetailTypes) {
                                                final RaceBoardPerspectiveLifecycle lifeCycle = new RaceBoardPerspectiveLifecycle(
                                                        StringMessages.INSTANCE, result, getUserService(), availableDetailTypes);
                                                RaceBoardComponentContext componentContext = new RaceBoardComponentContext(
                                                        lifeCycle, getUserService(), storageDefinition);

                                                componentContext.getInitialSettings(
                                                        new DefaultOnSettingsLoadedCallback<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>>() {
                                                            @Override
                                                            public void onSuccess(
                                                                    PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> initialSettings) {
                                                                Timer timer = new Timer(PlayModes.Replay, 1000l);
                                                                final RaceBoardPanel raceBoardPanel = createPerspectivePage(
                                                                        null, componentContext, initialSettings,
                                                                        raceboardData,
                                                                        showChartMarkEditMediaButtonsAndVideo,
                                                                        lifeCycle, timer, availableDetailTypes);
                                                                if (finalMode != null) {
                                                                    finalMode.getMode().applyTo(raceBoardPanel);
                                                                }
                                                            }
                                                        });
                                            }
                                        });
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
        final SAPHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication();
        new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        RootLayoutPanel.get().add(vp);
        vp.addNorth(header, 100);
        final Label infoText = new Label(message);
        infoText.getElement().getStyle().setMargin(1, Unit.EM);
        vp.add(infoText);
        // TODO: Styling of error page slightly differs from the other usages of SAPSailingHeaderWithAuthentication
        // because of the root font-size. Adjustments are postponed because they might affect the hole page content.
    }

    private RaceBoardPanel createPerspectivePage(Component<?> parent,
            ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> context,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings, RaceboardDataDTO raceboardData,
            boolean showChartMarkEditMediaButtonsAndVideo, RaceBoardPerspectiveLifecycle raceLifeCycle, Timer timer, Collection<DetailType> availableDetailTypes) {
        selectedRace = raceboardData.getRace();
        Window.setTitle(selectedRace.getName());
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
                raceTimesInfoProvider, showChartMarkEditMediaButtonsAndVideo, true, availableDetailTypes);
        RootLayoutPanel.get().add(raceBoardPerspective.getEntryWidget());
        return raceBoardPerspective;
    }  
}
