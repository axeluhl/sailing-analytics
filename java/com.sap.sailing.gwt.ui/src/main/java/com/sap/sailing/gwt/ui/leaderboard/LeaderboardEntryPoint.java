package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;


public class LeaderboardEntryPoint extends AbstractEntryPoint {
    private static final Logger logger = Logger.getLogger(LeaderboardEntryPoint.class.getName());
    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardGroupName";
    private static final String PARAM_EMBEDDED = "embedded";
    private static final String PARAM_SHOW_RACE_DETAILS = "showRaceDetails";
    private static final String PARAM_RACE_NAME = "raceName";
    private static final String PARAM_RACE_DETAIL = "raceDetail";
    private static final String PARAM_OVERALL_DETAIL = "overallDetail";
    private static final String PARAM_LEG_DETAIL = "legDetail";
    private static final String PARAM_MANEUVER_DETAIL = "maneuverDetail";
    private static final String PARAM_AUTO_EXPAND_PRESELECTED_RACE = "autoExpandPreselectedRace";
    private static final String PARAM_REGATTA_NAME = "regattaName";
    private static final String PARAM_REFRESH_INTERVAL_MILLIS = "refreshIntervalMillis";
    private static final String PARAM_DELAY_TO_LIVE_MILLIS = "delayToLiveMillis";
    private String leaderboardName;
    private String leaderboardGroupName;
    
    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        final boolean showRaceDetails = Window.Location.getParameter(PARAM_SHOW_RACE_DETAILS) != null
                && Window.Location.getParameter(PARAM_SHOW_RACE_DETAILS).equalsIgnoreCase("true");
        final boolean embedded = Window.Location.getParameter(PARAM_EMBEDDED) != null
                && Window.Location.getParameter(PARAM_EMBEDDED).equalsIgnoreCase("true");
        final long delayToLiveMillis = Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS) != null ?
                Long.valueOf(Window.Location.getParameter(PARAM_DELAY_TO_LIVE_MILLIS)) : 5000l; // default 5s
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                leaderboardName = Window.Location.getParameter("name");
                leaderboardGroupName = Window.Location.getParameter(PARAM_LEADERBOARD_GROUP_NAME);
                if (leaderboardNames.contains(leaderboardName)) {
                    createUI(showRaceDetails, embedded, delayToLiveMillis);
                } else {
                    RootPanel.get().add(new Label(stringMessages.noSuchLeaderboard()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: " + t.getMessage());
            }
        });
    }
    
    private void createUI(boolean showRaceDetails, boolean embedded, long delayToLiveMillis) {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        LogoAndTitlePanel logoAndTitlePanel = null;
        if (!embedded) {
            // Hack to shorten the leaderboardName in case of overall leaderboards
            String leaderboardDisplayName = Window.Location.getParameter("displayName");
            if(leaderboardDisplayName == null || leaderboardDisplayName.isEmpty()) {
                leaderboardDisplayName = leaderboardName;
            }
            logoAndTitlePanel = new LogoAndTitlePanel(leaderboardGroupName, leaderboardDisplayName, stringMessages);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            mainPanel.addNorth(logoAndTitlePanel, 68);
        }
        ScrollPanel contentScrollPanel = new ScrollPanel();
        
        String tvModeParam = Window.Location.getParameter("tvMode");
        if (tvModeParam != null) {
            Timer timer = new Timer(PlayModes.Live, 1000l);
            timer.setLivePlayDelayInMillis(delayToLiveMillis);
            TVViewPanel tvViewPanel = new TVViewPanel(sailingService, stringMessages, this, leaderboardName,
                    userAgent, null, timer, logoAndTitlePanel, mainPanel, showRaceDetails);
            contentScrollPanel.setWidget(tvViewPanel);
        } else {
            Timer timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
            timer.setLivePlayDelayInMillis(delayToLiveMillis);
            final LeaderboardSettings leaderboardSettings = createLeaderboardSettingsFromURLParameters(Window.Location.getParameterMap());
            if (leaderboardSettings.getDelayBetweenAutoAdvancesInMilliseconds() != null) {
                timer.setPlayMode(PlayModes.Live); // the leaderboard, viewed via the entry point, always goes "live"
            }
            LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, new AsyncActionsExecutor(),
                    leaderboardSettings,
                    getPreselectedRace(Window.Location.getParameterMap()), new CompetitorSelectionModel(
                            /* hasMultiSelection */true), timer, leaderboardName, leaderboardGroupName,
                    LeaderboardEntryPoint.this, stringMessages, userAgent, showRaceDetails);
            contentScrollPanel.setWidget(leaderboardPanel);
        }

        mainPanel.add(contentScrollPanel);
    }
    
    private RaceIdentifier getPreselectedRace(Map<String, List<String>> parameterMap) {
        RaceIdentifier result;
        if (parameterMap.containsKey(PARAM_RACE_NAME) && parameterMap.get(PARAM_RACE_NAME).size() == 1 &&
                parameterMap.containsKey(PARAM_REGATTA_NAME) && parameterMap.get(PARAM_REGATTA_NAME).size() == 1) {
            result = new RegattaNameAndRaceName(parameterMap.get(PARAM_REGATTA_NAME).get(0), parameterMap.get(PARAM_RACE_NAME).get(0));
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Constructs {@link LeaderboardSettings} from the URL parameters found
     */
    private LeaderboardSettings createLeaderboardSettingsFromURLParameters(Map<String, List<String>> parameterMap) {
        LeaderboardSettings result;
        Long refreshIntervalMillis = parameterMap.containsKey(PARAM_REFRESH_INTERVAL_MILLIS) ?
                Long.valueOf(parameterMap.get(PARAM_REFRESH_INTERVAL_MILLIS).get(0)) : null;
        if (parameterMap.containsKey(PARAM_RACE_NAME) || parameterMap.containsKey(PARAM_RACE_DETAIL) ||
                parameterMap.containsKey(PARAM_LEG_DETAIL) || parameterMap.containsKey(PARAM_MANEUVER_DETAIL) ||
                parameterMap.containsKey(PARAM_OVERALL_DETAIL)) {
            List<DetailType> maneuverDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_MANEUVER_DETAIL));
            List<DetailType> raceDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_RACE_DETAIL));
            List<DetailType> overallDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_OVERALL_DETAIL));
            List<DetailType> legDetails = getDetailTypeListFromParamValue(parameterMap.get(PARAM_MANEUVER_DETAIL));
            List<String> namesOfRacesToShow = getStringListFromParamValue(parameterMap.get(PARAM_RACE_NAME));
            boolean autoExpandPreSelectedRace = parameterMap.containsKey(PARAM_AUTO_EXPAND_PRESELECTED_RACE) ?
                    Boolean.valueOf(parameterMap.get(PARAM_AUTO_EXPAND_PRESELECTED_RACE).get(0)) :
                        (namesOfRacesToShow != null && namesOfRacesToShow.size() == 1);
            result = new LeaderboardSettings(maneuverDetails, legDetails, raceDetails, overallDetails,
                    /* namesOfRaceColumnsToShow */ null,
                    namesOfRacesToShow, autoExpandPreSelectedRace,
                    refreshIntervalMillis, /* delay to live */ null, /* sort by column */ (namesOfRacesToShow != null && !namesOfRacesToShow.isEmpty()) ?
                                    namesOfRacesToShow.get(0) : null,
                            /* ascending */ true, /* updateUponPlayStateChange */ raceDetails.isEmpty() && legDetails.isEmpty());
        } else {
            final List<DetailType> overallDetails = Collections.emptyList();
            result = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, /* overallDetails */
                    overallDetails, null,
                    /* autoExpandFirstRace */ false, refreshIntervalMillis);
        }
        return result;
    }

    private List<DetailType> getDetailTypeListFromParamValue(List<String> list) {
        List<DetailType> result = new ArrayList<DetailType>();
        if (list != null) {
            for (String entry : list) {
                try {
                    result.add(DetailType.valueOf(entry));
                } catch (IllegalArgumentException e) {
                    logger.info("Can't find detail type "+entry+". Ignoring.");
                }
            }
        }
        return result;
    }

    private List<String> getStringListFromParamValue(List<String> list) {
        List<String> result = new ArrayList<String>();
        if (list != null) {
            result.addAll(list);
        }
        return result;
    }
}
