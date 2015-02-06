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
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.GlobalNavigationPanel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;


public class LeaderboardEntryPoint extends AbstractSailingEntryPoint {
    private String leaderboardName;
    private String leaderboardGroupName;
    private LeaderboardType leaderboardType;
    private GlobalNavigationPanel globalNavigationPanel;
    private EventDTO event;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        final boolean showRaceDetails = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_SHOW_RACE_DETAILS, false /* default*/);
        final boolean embedded = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_EMBEDDED, false /* default*/); 
        final boolean hideToolbar = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_HIDE_TOOLBAR, false /* default*/); 
        final String eventIdAsString = GwtHttpRequestUtils.getStringParameter(LeaderboardUrlSettings.PARAM_EVENT_ID, null /* default*/);
        final UUID eventId = eventIdAsString == null ? null : UUID.fromString(eventIdAsString);

        leaderboardName = Window.Location.getParameter("name");
        leaderboardGroupName = Window.Location.getParameter(LeaderboardUrlSettings.PARAM_LEADERBOARD_GROUP_NAME);

        if (leaderboardName != null) {
            final Runnable checkLeaderboardNameAndCreateUI = new Runnable() {
                @Override
                public void run() {
                    sailingService.checkLeaderboardName(leaderboardName,
                            new MarkedAsyncCallback<Util.Pair<String, LeaderboardType>>(
                                    new AsyncCallback<Util.Pair<String, LeaderboardType>>() {
                                        @Override
                                        public void onSuccess(Util.Pair<String, LeaderboardType> leaderboardNameAndType) {
                                            if (leaderboardNameAndType != null
                                                    && leaderboardName.equals(leaderboardNameAndType.getA())) {
                                                Window.setTitle(leaderboardName);
                                                leaderboardType = leaderboardNameAndType.getB();
                                                createUI(showRaceDetails, embedded, hideToolbar, event);
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
                sailingService.getEventById(eventId, /* withStatisticalData */false, new MarkedAsyncCallback<EventDTO>(
                        new AsyncCallback<EventDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                reportError("Error trying to obtain event "+eventId+": " + caught.getMessage());
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
        final String zoomTo = Window.Location.getParameter(LeaderboardUrlSettings.PARAM_ZOOM_TO);
        if (zoomTo != null) {
            RootPanel.getBodyElement().setAttribute(
                    "style",
                    "zoom: " + zoomTo + ";-moz-transform: scale(" + zoomTo
                            + ");-moz-transform-origin: 0 0;-o-transform: scale(" + zoomTo
                            + ");-o-transform-origin: 0 0;-webkit-transform: scale(" + zoomTo
                            + ");-webkit-transform-origin: 0 0;");
        }
    }
    
    private void createUI(boolean showRaceDetails, boolean embedded, boolean hideToolbar, EventDTO event) {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        LogoAndTitlePanel logoAndTitlePanel = null;
        if (!embedded) {
            // Hack to shorten the leaderboardName in case of overall leaderboards
            String leaderboardDisplayName = Window.Location.getParameter("displayName");
            if (leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
                leaderboardDisplayName = leaderboardName;
            }
            globalNavigationPanel = new GlobalNavigationPanel(getStringMessages(), true, null, leaderboardGroupName, event, null);
            logoAndTitlePanel = new LogoAndTitlePanel(leaderboardGroupName, leaderboardDisplayName, getStringMessages(), this, getUserService()) {
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
            mainPanel.addNorth(logoAndTitlePanel, 68);
        }
        ScrollPanel contentScrollPanel = new ScrollPanel();
        long delayBetweenAutoAdvancesInMilliseconds = 3000l;
        final RaceIdentifier preselectedRace = getPreselectedRace(Window.Location.getParameterMap());
        // make a single live request as the default but don't continue to play by default
        Timer timer = new Timer(PlayModes.Live, PlayStates.Paused, delayBetweenAutoAdvancesInMilliseconds);
        final LeaderboardSettings leaderboardSettings = LeaderboardSettings.createLeaderboardSettingsFromURLParameters(Window.Location.getParameterMap());
        if (leaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
            timer.setPlayMode(PlayModes.Live); // the leaderboard, viewed via the entry point, goes "live" and "playing" if an auto-refresh
        } // interval has been specified
        boolean autoExpandLastRaceColumn = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_AUTO_EXPAND_LAST_RACE_COLUMN, false);
        boolean showCharts = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_SHOW_CHARTS, false);
        boolean showOverallLeaderboard = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_SHOW_OVERALL_LEADERBOARD, false);
        boolean showSeriesLeaderboards = GwtHttpRequestUtils.getBooleanParameter(LeaderboardUrlSettings.PARAM_SHOW_SERIES_LEADERBOARDS, false);
        String chartDetailParam = GwtHttpRequestUtils.getStringParameter(LeaderboardUrlSettings.PARAM_CHART_DETAIL, null);
        final DetailType chartDetailType;
        if (chartDetailParam != null && (DetailType.REGATTA_RANK.name().equals(chartDetailParam) || DetailType.OVERALL_RANK.name().equals(chartDetailParam) || 
                DetailType.REGATTA_TOTAL_POINTS_SUM.name().equals(chartDetailParam))) {
            chartDetailType = DetailType.valueOf(chartDetailParam);
        } else {
            chartDetailType = leaderboardType.isMetaLeaderboard() ?  DetailType.OVERALL_RANK : DetailType.REGATTA_RANK;
        }
        
        final Widget leaderboardViewer;
        if (leaderboardType.isMetaLeaderboard()) {
            leaderboardViewer = new MetaLeaderboardViewer(sailingService, new AsyncActionsExecutor(),
                    timer, leaderboardSettings, null, preselectedRace, leaderboardGroupName, leaderboardName, this, getStringMessages(),
                    userAgent, showRaceDetails, hideToolbar, autoExpandLastRaceColumn, showCharts, chartDetailType, showSeriesLeaderboards);
        } else {
            leaderboardViewer = new LeaderboardViewer(sailingService, new AsyncActionsExecutor(),
                    timer, leaderboardSettings, preselectedRace, leaderboardGroupName, leaderboardName, this, getStringMessages(),
                    userAgent, showRaceDetails, hideToolbar, autoExpandLastRaceColumn, showCharts, chartDetailType, showOverallLeaderboard);
        }
        contentScrollPanel.setWidget(leaderboardViewer);
        mainPanel.add(contentScrollPanel);
    }
   
    private RaceIdentifier getPreselectedRace(Map<String, List<String>> parameterMap) {
        RaceIdentifier result;
        if (parameterMap.containsKey(LeaderboardUrlSettings.PARAM_RACE_NAME) && parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).size() == 1 &&
                parameterMap.containsKey(LeaderboardUrlSettings.PARAM_REGATTA_NAME) && parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).size() == 1) {
            result = new RegattaNameAndRaceName(parameterMap.get(LeaderboardUrlSettings.PARAM_REGATTA_NAME).get(0), parameterMap.get(LeaderboardUrlSettings.PARAM_RACE_NAME).get(0));
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Assembles a dialog that other parts of the application can use to let the user parameterize a leaderboard and
     * obtain the according URL for it. This keeps the "secrets" of which URL parameters have which meaning encapsulated
     * within this class.<p>
     * 
     * The implementation by and large uses the {@link LeaderboardSettingsDialogComponent}'s widget and adds to it a checkbox
     * for driving the {@link #LeaderboardUrlSettings.PARAM_EMBEDDED} field.
     * 
     * @see LeaderboardEntryPoint#getUrl(String, LeaderboardSettings, boolean)
     */
    public static DataEntryDialog<LeaderboardUrlSettings> getUrlConfigurationDialog(final AbstractLeaderboardDTO leaderboard,
            final StringMessages stringMessages) {
        return new LeaderboardUrlConfigurationDialog(stringMessages, leaderboard);
    }

}
