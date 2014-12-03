package com.sap.sailing.dashboards.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.device.Location;
import com.sap.sailing.dashboards.gwt.client.popups.RacingNotYetStartedPopup;
import com.sap.sailing.dashboards.gwt.client.popups.RacingNotYetStartedPopupListener;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionPopup;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionPopupListener;
import com.sap.sailing.dashboards.gwt.client.startanalysis.NewStartAnalysisListener;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;

/**
 * Class calls method {@link #requestLiveRaceInfoFromRibDashboadService()) to retrieve new
 * data for the dashboard widgets on a regularly basis.
 * The request interval is 5 seconds.
 * 
 * @author Alexander Ries
 * 
 */
public class RibDashboardDataRetriever implements RacingNotYetStartedPopupListener, CompetitorSelectionPopupListener{

    private int numberOfChachedStartAnalysisDTOs;
    private String leaderboardGroupName;
    private Timer dataRetrieverTimer;
    private ArrayList<RibDashboardDataRetrieverListener> dataRetrieverListener;
    private ArrayList<NewStartAnalysisListener> newStartAnalysisListeners;

    private final RibDashboardServiceAsync ribDashboardService;
    private final Object MUTEX = new Object();

    private static RibDashboardDataRetriever INSTANCE = null;

    private static final String PARAM_LEADERBOARD_GROUP_NAME = "leaderboardName";
    private static final int REQUEST_INTERVAL = 5000;
    
    private String selectedTeamName;
    
    private CompetitorSelectionPopup competitorSelectionPopup;
    private RacingNotYetStartedPopup popupRacingNotYetStarted;

    public RibDashboardDataRetriever() {
        dataRetrieverListener = new ArrayList<RibDashboardDataRetrieverListener>();
        newStartAnalysisListeners = new ArrayList<NewStartAnalysisListener>();
        ribDashboardService = GWT.create(RibDashboardService.class);
        this.leaderboardGroupName = Window.Location.getParameter(PARAM_LEADERBOARD_GROUP_NAME);
        numberOfChachedStartAnalysisDTOs = 0;
        popupRacingNotYetStarted = new RacingNotYetStartedPopup();
        popupRacingNotYetStarted.addListener(this);
        startRequestingData();
    }

    public static RibDashboardDataRetriever getInstance() {
        synchronized (Location.class) {
            if (INSTANCE == null) {
                INSTANCE = new RibDashboardDataRetriever();
            }
        }
        return INSTANCE;
    }

    public void startRequestingData() {
        if (dataRetrieverTimer == null) {

            dataRetrieverTimer = new Timer() {
                @Override
                public void run() {
                    requestLiveRaceInfoFromRibDashboadService();
                }
            };
            dataRetrieverTimer.scheduleRepeating(REQUEST_INTERVAL);
            dataRetrieverTimer.run();
        }
    }

    private void stopRequestingData() {
        if (dataRetrieverTimer.isRunning()) {
            dataRetrieverTimer.cancel();
            dataRetrieverTimer = null;
        }
    }

    private void requestLiveRaceInfoFromRibDashboadService() {
    	System.out.println("Request with team "+selectedTeamName);
        ribDashboardService.getLiveRaceInfo(leaderboardGroupName, selectedTeamName,
                new AsyncCallback<RibDashboardRaceInfoDTO>() {
                    @Override
                    public void onSuccess(RibDashboardRaceInfoDTO result) {
                        switch (result.responseMessage) {
                        case OK:
                            popupRacingNotYetStarted.hide(true);
                            if (result.startAnalysisDTOList != null) {
                                int numberOfReceivedStartAnalysisDTOs = result.startAnalysisDTOList.size();
                                if (numberOfChachedStartAnalysisDTOs != numberOfReceivedStartAnalysisDTOs) {
                                    numberOfChachedStartAnalysisDTOs = numberOfReceivedStartAnalysisDTOs;
                                    notifyNewStartAnalysisListener(result.startAnalysisDTOList);
                                }
                            }
                            notifyDataObservers(result);
                            break;
                        case NO_COMPETITOR_SELECTED:
                            popupRacingNotYetStarted.hide(true);
                            if (result.competitorNamesFromLastTrackedRace != null
                                    && result.competitorNamesFromLastTrackedRace.size() > 0) {
                                competitorSelectionPopup = new CompetitorSelectionPopup(
                                        result.competitorNamesFromLastTrackedRace);
                                competitorSelectionPopup.addListener(RibDashboardDataRetriever.this);
                                competitorSelectionPopup.show();
                                stopRequestingData();
                            }
                            break;
                        case NO_RACE_LIVE:
                            popupRacingNotYetStarted.showWithMessageAndImageAndButtonText("Racing not yet started", RibDashboardImageResources.INSTANCE.beach(), "Retry");
                            stopRequestingData();
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
        startRequestingData();
    }

    @Override
    public void didClickedOKWithCompetitorName(String competitorName) {
    	System.out.println("Did clicked with competitor "+competitorName);
        this.selectedTeamName = competitorName;
        startRequestingData();
    }
}
