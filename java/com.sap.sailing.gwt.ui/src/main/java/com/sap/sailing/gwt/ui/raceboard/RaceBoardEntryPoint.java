
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
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.settings.client.utils.StoredSettingsLocationFactory;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsAndBoatsDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.DefaultOnSettingsLoadedCallback;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorage;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

public class RaceBoardEntryPoint extends AbstractSailingEntryPoint implements ProvidesLeaderboardRouting {

    /**
     * Controls the predefined mode into which to switch or configure the race viewer.
     */
    private final MediaServiceAsync mediaService = GWT.create(MediaService.class);
    private RaceboardContextDefinition raceboardContextDefinition;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaService,
                RemoteServiceMappingConstants.mediaServiceRemotePath);
        raceboardContextDefinition = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new RaceboardContextDefinition());
        if (raceboardContextDefinition.getRegattaName() == null || raceboardContextDefinition.getRegattaName().isEmpty()
                || raceboardContextDefinition.getRaceName() == null
                || raceboardContextDefinition.getRaceName().isEmpty()
                || raceboardContextDefinition.getLeaderboardName() == null
                || raceboardContextDefinition.getLeaderboardName().isEmpty()) {
            createErrorPage(getStringMessages().requiresRegattaRaceAndLeaderboard());
        } else {
            getSailingService().getRaceboardData(raceboardContextDefinition.getRegattaName(),
                    raceboardContextDefinition.getRaceName(), raceboardContextDefinition.getLeaderboardName(),
                    raceboardContextDefinition.getLeaderboardGroupName(), raceboardContextDefinition.getEventId(),
                    new AbstractRaceBoardAsyncCallback<RaceboardDataDTO>() {
                        @Override
                        public void onSuccess(RaceboardDataDTO raceboardData) {
                            startWithRaceboardData(raceboardData);
                        }
                    });
        }
    }
    
    private void startWithRaceboardData(RaceboardDataDTO raceboardData) {
        final String modeString = raceboardContextDefinition.getMode();
        final RaceBoardModes mode = modeString == null ? null : RaceBoardModes.valueOf(modeString);
        if (raceboardData.getLeaderboard() == null) {
            createErrorPage(getStringMessages().noSuchLeaderboard());
            return;
        }
        if (raceboardContextDefinition.getEventId() != null && !raceboardData.isValidEvent()) {
            createErrorPage(getStringMessages().noSuchEvent());
            return;
        }
        if (raceboardContextDefinition.getLeaderboardGroupName() != null) {
            if (!raceboardData.isValidLeaderboardGroup()) {
                createErrorPage(getStringMessages().leaderboardNotContainedInLeaderboardGroup(
                        raceboardContextDefinition.getLeaderboardName(),
                        raceboardContextDefinition.getLeaderboardGroupName()));
                return;
            } else if (raceboardContextDefinition.getEventId() != null && raceboardData.isValidLeaderboardGroup()
                    && !raceboardData.isValidEvent()) {
                createErrorPage(getStringMessages().leaderboardGroupNotContainedInEvent(
                        raceboardContextDefinition.getLeaderboardGroupName(),
                        raceboardContextDefinition.getEventId().toString()));
                return;
            }
        }
        if (raceboardData.getRace() == null) {
            createErrorPage(getStringMessages().couldNotFindRaceInRegatta(raceboardContextDefinition.getRaceName(),
                    raceboardContextDefinition.getRegattaName()));
            return;
        }
        final StoredSettingsLocation storageDefinition = StoredSettingsLocationFactory
                .createStoredSettingsLocatorForRaceBoard(raceboardContextDefinition, mode != null ? mode.name() : null);
        final RaceBoardPerspectiveLifecycle lifeCycle = new RaceBoardPerspectiveLifecycle(
                raceboardData.getLeaderboard(), StringMessages.INSTANCE,
                raceboardData.getDetailTypesForCompetitorChart(), getUserService(),
                raceboardData.getAvailableDetailTypesForLeaderboard());
        RaceBoardComponentContext componentContext = new RaceBoardComponentContext(lifeCycle, getUserService(),
                storageDefinition);

        componentContext.getInitialSettings(
                new DefaultOnSettingsLoadedCallback<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>>() {
                    @Override
                    public void onSuccess(PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings) {
                        createPerspective(mode, componentContext, settings, raceboardData, lifeCycle,
                                raceboardData.getAvailableDetailTypesForLeaderboard());
                    }
                });
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
        // because of the root font-size. Adjustments are postponed because they might affect the whole page content.
    }

    private void createPerspective(final RaceBoardModes raceBoardMode,
            ComponentContextWithSettingsStorage<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> context,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings, RaceboardDataDTO raceboardData,
            RaceBoardPerspectiveLifecycle raceLifeCycle, Iterable<DetailType> availableDetailTypes) {
        final Timer timer = new Timer(PlayModes.Replay, 1000l);
        final boolean showChartMarkEditMediaButtonsAndVideo = !DeviceDetector.isMobile();

        final RaceWithCompetitorsAndBoatsDTO selectedRace = raceboardData.getRace();
        Window.setTitle(selectedRace.getName());
        AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
        RaceTimesInfoProvider raceTimesInfoProvider = new RaceTimesInfoProvider(getSailingService(),
                asyncActionsExecutor, this, Collections.singletonList(selectedRace.getRaceIdentifier()),
                5000l /* requestInterval */);
        RaceBoardPanel raceBoardPerspective = new RaceBoardPanel(/* parent */ null, context, raceLifeCycle, settings,
                getSailingService(), mediaService, getUserService(), asyncActionsExecutor,
                raceboardData.getCompetitorAndTheirBoats(), timer, selectedRace.getRaceIdentifier(),
                raceboardContextDefinition.getLeaderboardName(), raceboardContextDefinition.getLeaderboardGroupName(),
                raceboardContextDefinition.getEventId(), RaceBoardEntryPoint.this, getStringMessages(), userAgent,
                raceTimesInfoProvider, showChartMarkEditMediaButtonsAndVideo, true, availableDetailTypes,
                raceboardData.getLeaderboard());
        RootLayoutPanel.get().add(raceBoardPerspective.getEntryWidget());

        if (raceBoardMode != null) {
            raceBoardMode.getMode().applyTo(raceBoardPerspective);
            raceBoardMode.getMode().addInitializationFinishedRunner(
                    () -> selectCompetitorFromPerspectiveOwnSetting(raceBoardPerspective));
        } else {
            selectCompetitorFromPerspectiveOwnSetting(raceBoardPerspective);
        }
    }  
    
    protected void selectCompetitorFromPerspectiveOwnSetting(RaceBoardPanel raceBoardPanel) {
        RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = raceBoardPanel.getSettings()
                .getPerspectiveOwnSettings();
        if (perspectiveOwnSettings != null) {
            String competitorId = perspectiveOwnSettings.getSelectedCompetitor();
            if (competitorId != null && !"".equals(competitorId)) {
                for (CompetitorDTO comp : raceBoardPanel.getCompetitorSelectionProvider().getAllCompetitors()) {
                    if (competitorId.equals(comp.getIdAsString())) {
                        raceBoardPanel.getCompetitorSelectionProvider().setSelected(comp, true,
                                new CompetitorSelectionChangeListener[0]);
                    } else {
                        raceBoardPanel.getCompetitorSelectionProvider().setSelected(comp, false,
                                new CompetitorSelectionChangeListener[0]);
                    }
                }
            }
        }
    }

    @Override
    public String getLeaderboardName() {
        return raceboardContextDefinition.getLeaderboardName();
    }

    private abstract class AbstractRaceBoardAsyncCallback<T> implements AsyncCallback<T> {
        @Override
        public final void onFailure(Throwable caught) {
            reportError("Error trying to create the raceboard: " + caught.getMessage());
        }
    }
}
