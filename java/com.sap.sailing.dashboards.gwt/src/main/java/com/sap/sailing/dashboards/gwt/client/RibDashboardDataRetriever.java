package com.sap.sailing.dashboards.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.dashboards.gwt.client.bottomnotification.BottomNotification;
import com.sap.sailing.dashboards.gwt.client.device.Location;
import com.sap.sailing.dashboards.gwt.client.popups.RacingNotYetStartedPopup;
import com.sap.sailing.dashboards.gwt.client.popups.RacingNotYetStartedPopupListener;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionPopup;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionPopupListener;
import com.sap.sailing.dashboards.gwt.client.startanalysis.NewStartAnalysisListener;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sse.gwt.client.player.TimeListener;

/**
 * Class calls method {@link #requestLiveRaceInfoFromRibDashboadService()) to retrieve new
 * data for the dashboard widgets on a regularly basis.
 * The request interval is 5 seconds.
 * 
 * @author Alexander Ries
 * 
 */
public class RibDashboardDataRetriever implements RacingNotYetStartedPopupListener, CompetitorSelectionPopupListener,
        TimeListener, RaceSelectionProvider {

    private int numberOfChachedStartAnalysisDTOs;
    private String leaderboardGroupName;
    private ArrayList<RibDashboardDataRetrieverListener> dataRetrieverListener;
    private ArrayList<NewStartAnalysisListener> newStartAnalysisListeners;
    private ArrayList<RaceSelectionChangeListener> raceSelectionChangeListener;

    private final RibDashboardServiceAsync ribDashboardService;

    private final Object MUTEX;

    private static RibDashboardDataRetriever INSTANCE = null;

    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardName";
    private static final String KEY_SLECTED_TEAM_COOKIE = "selectedTeam";
    private static final int SLECTED_TEAM_COOKIE_EXPIRE_TIME_IN_MILLIS = 60*1000*60*5;

    private String selectedTeamName;

    private CompetitorSelectionPopup competitorSelectionPopup;
    private RacingNotYetStartedPopup popupRacingNotYetStarted;
    private BottomNotification bottomNotification;

    public RibDashboardDataRetriever() {
        initNonFinalMemberVariablesWithNoArgumentConstructor();
        initRacingNotYetStartedPopup();
        MUTEX = new Object();
        ribDashboardService = GWT.create(RibDashboardService.class);
        this.leaderboardGroupName = Window.Location.getParameter(PARAM_LEADERBOARD_GROUP_NAME);
        numberOfChachedStartAnalysisDTOs = 0;
        competitorSelectionPopup = new CompetitorSelectionPopup();
        selectedTeamName = Cookies.getCookie(KEY_SLECTED_TEAM_COOKIE);
        initBottomNotification();
    }

    public static RibDashboardDataRetriever getInstance() {
        synchronized (Location.class) {
            if (INSTANCE == null) {
                INSTANCE = new RibDashboardDataRetriever();
            }
        }
        return INSTANCE;
    }

    private void initRacingNotYetStartedPopup() {
        popupRacingNotYetStarted = new RacingNotYetStartedPopup();
        popupRacingNotYetStarted.addListener(this);
    }

    private void initBottomNotification() {
        bottomNotification = new BottomNotification();
        RootPanel.get().add(bottomNotification);
    }

    private void initNonFinalMemberVariablesWithNoArgumentConstructor() {
        dataRetrieverListener = new ArrayList<RibDashboardDataRetrieverListener>();
        newStartAnalysisListeners = new ArrayList<NewStartAnalysisListener>();
        raceSelectionChangeListener = new ArrayList<RaceSelectionChangeListener>();
    }

    private void loadLiveRaceInfoFromRibDashboadService() {
        ribDashboardService.getLiveRaceInfo(leaderboardGroupName, selectedTeamName,
                new AsyncCallback<RibDashboardRaceInfoDTO>() {
                    @Override
                    public void onSuccess(RibDashboardRaceInfoDTO result) {
                        switch (result.responseMessage) {
                        case OK:
                            popupRacingNotYetStarted.hide(/* remove blur effect */true);
                            List<RegattaAndRaceIdentifier> singletonList = Collections
                                    .singletonList(result.idOfLastTrackedRace);
                            setSelection(singletonList);
                            if (result.startAnalysisDTOList != null) {
                                int numberOfReceivedStartAnalysisDTOs = result.startAnalysisDTOList.size();
                                if (numberOfChachedStartAnalysisDTOs != numberOfReceivedStartAnalysisDTOs) {
                                    numberOfChachedStartAnalysisDTOs = numberOfReceivedStartAnalysisDTOs;
                                    notifyNewStartAnalysisListener(result.startAnalysisDTOList);
                                }
                            }
                            notifyDataObservers(result);
                            competitorSelectionPopup.hide();
                            break;
                        case NO_COMPETITOR_SELECTED:
                            popupRacingNotYetStarted.hide(true);
                            if (result.competitorNamesFromLastTrackedRace != null
                                    && result.competitorNamesFromLastTrackedRace.size() > 0 && !competitorSelectionPopup.isShown()) {
                                competitorSelectionPopup.setCompetitorList(result.competitorNamesFromLastTrackedRace);
                                competitorSelectionPopup.addListener(RibDashboardDataRetriever.this);
                                competitorSelectionPopup.show();
                            }
                            break;
                        case NO_RACE_LIVE:
                            if (numberOfChachedStartAnalysisDTOs == 0) {
                                popupRacingNotYetStarted.showWithMessageAndImageAndButtonText("Racing not yet started",
                                        RibDashboardImageResources.INSTANCE.beach(), "Retry");
                            } else {
                                bottomNotification.show("Race finished!", "#418bcb", "#FFFFFF", false);
                                setSelection(null);
                            }
                            break;
                        default:
                            break;
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                    }
                });
    }

    public void addDataObserver(RibDashboardDataRetrieverListener o) {
        synchronized (MUTEX) {
            if (o != null) {
                dataRetrieverListener.add(o);
            }
        }
    }

    public void removeDataObserver(RibDashboardDataRetrieverListener o) {
        synchronized (MUTEX) {
            this.dataRetrieverListener.remove(o);
        }
    }

    public void notifyDataObservers(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        List<RibDashboardDataRetrieverListener> dataObserverCopy;
        synchronized (MUTEX) {
            dataObserverCopy = new ArrayList<RibDashboardDataRetrieverListener>(dataRetrieverListener);
        }
        for (RibDashboardDataRetrieverListener o : dataObserverCopy) {
            o.updateUIWithNewLiveRaceInfo(liveRaceInfoDTO);
        }
    }

    public void addNewStartAnalysisListener(NewStartAnalysisListener o) {
        synchronized (MUTEX) {
            if (o != null && !newStartAnalysisListeners.contains(o)) {
                this.newStartAnalysisListeners.add(o);
            }
        }
    }

    public void removeNewStartAnalysisListener(NewStartAnalysisListener o) {
        synchronized (MUTEX) {
            this.newStartAnalysisListeners.remove(o);
        }
    }

    public void notifyNewStartAnalysisListener(List<StartAnalysisDTO> startAnalysisDTOs) {
        synchronized (MUTEX) {
            for (NewStartAnalysisListener newStartAnalysisListener : newStartAnalysisListeners) {
                newStartAnalysisListener.addNewStartAnalysisCard(startAnalysisDTOs);
            }
        }
    }

    @Override
    public void popupButtonClicked() {
    }

    @Override
    public void didClickedOKWithCompetitorName(String competitorName) {
        System.out.println("Did clicked with competitor " + competitorName);
        this.selectedTeamName = competitorName;
        Cookies.setCookie(KEY_SLECTED_TEAM_COOKIE, competitorName, new Date(new Date().getTime()+SLECTED_TEAM_COOKIE_EXPIRE_TIME_IN_MILLIS));
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadLiveRaceInfoFromRibDashboadService();
    }

    @Override
    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        synchronized (MUTEX) {
            if (listener != null) {
                raceSelectionChangeListener.add(listener);
            }
        }
    }

    @Override
    public void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        synchronized (MUTEX) {
            raceSelectionChangeListener.remove(listener);
        }
    }

    @Override
    public void setSelection(List<RegattaAndRaceIdentifier> newSelection,
            RaceSelectionChangeListener... listenersNotToNotify) {
        synchronized (MUTEX) {
            for (RaceSelectionChangeListener currentRaceSelectionChangeListener : raceSelectionChangeListener) {
                currentRaceSelectionChangeListener.onRaceSelectionChange(newSelection);
            }
        }
    }

    @Override
    public List<RegattaAndRaceIdentifier> getAllRaces() {
        return null;
    }

    @Override
    public void setAllRaces(List<RegattaAndRaceIdentifier> newAllRaces,
            RaceSelectionChangeListener... listenersNotToNotify) {
    }

    @Override
    public List<RegattaAndRaceIdentifier> getSelectedRaces() {
        return null;
    }
}
