package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChartSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogForLinkSharing;
import com.sap.sse.gwt.client.shared.perspective.AbstractComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.settings.UserSettingsStorageManager;

public class LeaderboardEntryPoint extends AbstractSailingEntryPoint {
    public static final long DEFAULT_REFRESH_INTERVAL_MILLIS = 3000l;

    private String leaderboardName;
    private String leaderboardGroupName;
    private LeaderboardType leaderboardType;
    private EventDTO event;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

        final LeaderboardContextSettings leaderboardContextSettings = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new LeaderboardContextSettings());

        final UUID eventId = leaderboardContextSettings.getEventId();

        leaderboardName = leaderboardContextSettings.getLeaderboardName();
        leaderboardGroupName = leaderboardContextSettings.getLeaderboardGroupName();

        if (leaderboardName != null) {
            final Runnable checkLeaderboardNameAndCreateUI = new Runnable() {
                @Override
                public void run() {
                    sailingService.checkLeaderboardName(leaderboardName,
                            new MarkedAsyncCallback<Util.Pair<String, LeaderboardType>>(
                                    new AsyncCallback<Util.Pair<String, LeaderboardType>>() {
                                        @Override
                                        public void onSuccess(
                                                Util.Pair<String, LeaderboardType> leaderboardNameAndType) {
                                            if (leaderboardNameAndType != null
                                                    && leaderboardName.equals(leaderboardNameAndType.getA())) {
                                                Window.setTitle(leaderboardName);
                                                leaderboardType = leaderboardNameAndType.getB();
                                                loadSettingsAndCreateUI(leaderboardContextSettings, event);
                                            } else {
                                                RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                            reportError("Error trying to obtain list of leaderboard names: "
                                                    + t.getMessage());
                                        }
                                    }));
                }
            };
            if (eventId == null) {
                checkLeaderboardNameAndCreateUI.run(); // use null-initialized event field
            } else {
                sailingService.getEventById(eventId, /* withStatisticalData */false,
                        new MarkedAsyncCallback<EventDTO>(new AsyncCallback<EventDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                reportError("Error trying to obtain event " + eventId + ": " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(EventDTO result) {
                                event = result;
                                checkLeaderboardNameAndCreateUI.run();
                            }
                        }));
            }
        } else {
            RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
        }
    }

    private void loadSettingsAndCreateUI(LeaderboardContextSettings leaderboardContextSettings, EventDTO event) {
        long delayBetweenAutoAdvancesInMilliseconds = DEFAULT_REFRESH_INTERVAL_MILLIS;
        final Timer timer = new Timer(PlayModes.Live, PlayStates.Paused, delayBetweenAutoAdvancesInMilliseconds);
        
        final RegattaAndRaceIdentifier preselectedRace = getPreselectedRace(Window.Location.getParameterMap());
        // make a single live request as the default but don't continue to play by default

        final UserSettingsStorageManager<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> settingsManager = new UserSettingsStorageManager<>(
                getUserService(), UserSettingsStorageManager.buildContextDefinitionId("LeaderboardEntryPoint"),
                leaderboardName);
        if (leaderboardType.isMetaLeaderboard()) {
            // overall

            MetaLeaderboardPerspectiveLifecycle rootComponentLifeCycle = new MetaLeaderboardPerspectiveLifecycle(
                    StringMessages.INSTANCE);
            AbstractComponentContextWithSettingsStorage<MetaLeaderboardPerspectiveLifecycle, PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> context = new AbstractComponentContextWithSettingsStorage<>(
                    rootComponentLifeCycle,
                    settingsManager);
            context.initInitialSettings(
                    new OnSettingsLoadedCallback<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>>() {
                        @Override
                        public void onSuccess(
                                PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> defaultSettings) {
                            configureWithSettings(defaultSettings, timer);
                            
                            final MetaLeaderboardViewer leaderboardViewer = new MetaLeaderboardViewer(null, context,
                                    rootComponentLifeCycle, defaultSettings, sailingService, new AsyncActionsExecutor(),
                                    timer, null, preselectedRace, leaderboardGroupName, leaderboardName,
                                    LeaderboardEntryPoint.this, getStringMessages(), userAgent, getActualChartDetailType(defaultSettings));
                            createUi(leaderboardViewer, defaultSettings, timer, leaderboardContextSettings);
                        }

                        @Override
                        public void onError(Throwable caught,
                                PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> fallbackDefaultSettings) {
                            // TODO
                            onSuccess(fallbackDefaultSettings);
                        }
                    });
        } else {

            LeaderboardPerspectiveLifecycle rootComponentLifeCycle = new LeaderboardPerspectiveLifecycle(
                    StringMessages.INSTANCE);
            AbstractComponentContextWithSettingsStorage<LeaderboardPerspectiveLifecycle, PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> context = new AbstractComponentContextWithSettingsStorage<>(
                    rootComponentLifeCycle,
                    settingsManager);
            context.initInitialSettings(
                    new OnSettingsLoadedCallback<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>>() {
                        @Override
                        public void onSuccess(
                                PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> defaultSettings) {
                            configureWithSettings(defaultSettings, timer);
                            
                            final LeaderboardViewer leaderboardViewer = new LeaderboardViewer(null, context,
                                    rootComponentLifeCycle, defaultSettings, sailingService, new AsyncActionsExecutor(),
                                    timer, preselectedRace, leaderboardGroupName, leaderboardName,
                                    LeaderboardEntryPoint.this, getStringMessages(), userAgent, getActualChartDetailType(defaultSettings));
                            createUi(leaderboardViewer, defaultSettings, timer, leaderboardContextSettings);
                        }

                        @Override
                        public void onError(Throwable caught,
                                PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> fallbackDefaultSettings) {
                            // TODO
                            onSuccess(fallbackDefaultSettings);
                        }
                    });
        }
        
    }
    
    private DetailType getActualChartDetailType(PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings) {
        MultiCompetitorLeaderboardChartSettings chartSettings = settings.findSettingsByComponentId(MultiCompetitorLeaderboardChartLifecycle.ID);
        DetailType chartDetailType = chartSettings == null ? null : chartSettings.getDetailType();
        
        if (chartDetailType == DetailType.REGATTA_RANK
                || chartDetailType == DetailType.OVERALL_RANK
                || chartDetailType == DetailType.REGATTA_NET_POINTS_SUM) {
            return chartDetailType;
        }
        return leaderboardType.isMetaLeaderboard() ? DetailType.OVERALL_RANK : DetailType.REGATTA_RANK;
    }
    
    private void createUi(Widget leaderboardViewer, PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings, Timer timer, LeaderboardContextSettings leaderboardContextSettings) {
        LeaderboardPerspectiveOwnSettings ownSettings = settings.getPerspectiveOwnSettings();
        
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        if (!ownSettings.isEmbedded()) {
            // Hack to shorten the leaderboardName in case of overall leaderboards
            String leaderboardDisplayName = leaderboardContextSettings.getDisplayName();
            if (leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
                leaderboardDisplayName = leaderboardName;
            }
            SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(leaderboardDisplayName);
            new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
            mainPanel.addNorth(header, 75);
        }

        mainPanel.add(new ScrollPanel(leaderboardViewer));
    }

    protected void configureWithSettings(
            PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings> settings, Timer timer) {
        LeaderboardPerspectiveOwnSettings perspectiveOwnSettings = settings.getPerspectiveOwnSettings();
        final String zoomTo = perspectiveOwnSettings.getZoomTo();
        if (zoomTo != null) {
            RootPanel.getBodyElement().setAttribute("style",
                    "zoom: " + zoomTo + ";-moz-transform: scale(" + zoomTo
                            + ");-moz-transform-origin: 0 0;-o-transform: scale(" + zoomTo
                            + ");-o-transform-origin: 0 0;-webkit-transform: scale(" + zoomTo
                            + ");-webkit-transform-origin: 0 0;");
        }
        
        LeaderboardSettings leaderboardSettings = settings.findSettingsByComponentId(LeaderboardPanelLifecycle.ID);
        if (leaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setPlayMode(PlayModes.Live); // the leaderboard, viewed via the entry point, goes "live" and "playing"
                                               // if an auto-refresh
        }
    }

    private RegattaAndRaceIdentifier getPreselectedRace(Map<String, List<String>> parameterMap) {
        RegattaAndRaceIdentifier result;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME)
                && parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).size() == 1
                && parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REGATTA_NAME)
                && parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).size() == 1) {
            result = new RegattaNameAndRaceName(parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).get(0),
                    parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).get(0));
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Assembles a dialog that other parts of the application can use to let the user parameterize a leaderboard and
     * obtain the according URL for it. This keeps the "secrets" of which URL parameters have which meaning encapsulated
     * within this class.
     * <p>
     * 
     * The implementation by and large uses the {@link LeaderboardSettingsDialogComponent}'s widget and adds to it a
     * checkbox for driving the {@link #LeaderboardUrlSettings.PARAM_EMBEDDED} field.
     * 
     * @see LeaderboardEntryPoint#getUrl(String, LeaderboardSettings, boolean)
     */
    public static void openUrlConfigurationDialog(final AbstractLeaderboardDTO leaderboard) {
        final AbstractLeaderboardPerspectiveLifecycle lifeCycle;
        if (leaderboard.type.isMetaLeaderboard()) {
            lifeCycle = new MetaLeaderboardPerspectiveLifecycle(StringMessages.INSTANCE, leaderboard);
        } else {
            lifeCycle = new LeaderboardPerspectiveLifecycle(StringMessages.INSTANCE, leaderboard);
        }
        final LeaderboardContextSettings leaderboardContextSettings = new LeaderboardContextSettings(leaderboard.name,
                leaderboard.getDisplayName());
        final LinkWithSettingsGenerator<PerspectiveCompositeSettings<LeaderboardPerspectiveOwnSettings>> linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(
                "/gwt/Leaderboard.html", leaderboardContextSettings);
        new SettingsDialogForLinkSharing<>(linkWithSettingsGenerator, lifeCycle, StringMessages.INSTANCE)
                .show();
    }
}
