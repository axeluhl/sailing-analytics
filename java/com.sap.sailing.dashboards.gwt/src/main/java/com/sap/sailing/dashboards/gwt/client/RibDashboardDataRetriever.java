package com.sap.sailing.dashboards.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.actions.GetRibDashboardRaceInfoAction;
import com.sap.sailing.dashboards.gwt.client.device.Location;
import com.sap.sailing.dashboards.gwt.client.popups.RacingNotYetStartedPopup;
import com.sap.sailing.dashboards.gwt.client.popups.RacingNotYetStartedPopupListener;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionPopup;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;

/**
 * Class calls method {@link #requestLiveRaceInfoFromRibDashboadService()) to retrieve new
 * data for the dashboard widgets on a regularly basis.
 * The request interval is 5 seconds.
 * 
 * @author Alexander Ries
 * 
 */
public class RibDashboardDataRetriever implements RacingNotYetStartedPopupListener,
        TimeListener, RaceSelectionProvider {

    private int numberOfCachedStartAnalysisDTOs;
    private String leaderboardName;
    private ArrayList<RibDashboardDataRetrieverListener> dataRetrieverListener;
    private ArrayList<RaceSelectionChangeListener> raceSelectionChangeListener;

    private CompetitorSelectionPopup competitorSelectionPopup;
    private RacingNotYetStartedPopup popupRacingNotYetStarted;
    
    private AsyncActionsExecutor asyncActionsExecutor;
    
    private final RibDashboardServiceAsync ribDashboardService;

    private final Object MUTEX;

    private static RibDashboardDataRetriever INSTANCE = null;

    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";

    public RibDashboardDataRetriever(RibDashboardServiceAsync ribDashboardService) {
        initNonFinalMemberVariablesWithNoArgumentConstructor();
        initRacingNotYetStartedPopup();
        MUTEX = new Object();
        this.ribDashboardService = ribDashboardService;
        this.leaderboardName = Window.Location.getParameter(PARAM_LEADERBOARD_NAME);
        numberOfCachedStartAnalysisDTOs = 0;
        competitorSelectionPopup = new CompetitorSelectionPopup();
        asyncActionsExecutor = new AsyncActionsExecutor();
    }

    public static RibDashboardDataRetriever getInstance(RibDashboardServiceAsync ribDashboardService) {
        synchronized (Location.class) {
            if (INSTANCE == null) {
                if (ribDashboardService == null) {
                    throw new RuntimeException("Can not initialize data retriever without reference to ribdashboard service");
                }
                INSTANCE = new RibDashboardDataRetriever(ribDashboardService);
            }
        }
        return INSTANCE;
    }

    private void initRacingNotYetStartedPopup() {
        popupRacingNotYetStarted = new RacingNotYetStartedPopup();
        popupRacingNotYetStarted.addListener(this);
    }

    private void initNonFinalMemberVariablesWithNoArgumentConstructor() {
        dataRetrieverListener = new ArrayList<RibDashboardDataRetrieverListener>();
        raceSelectionChangeListener = new ArrayList<RaceSelectionChangeListener>();
    }

    private void loadLiveRaceInfoFromRibDashboadService() {
        
        GetRibDashboardRaceInfoAction getRibDashboardRaceInfoAction = new GetRibDashboardRaceInfoAction(ribDashboardService, leaderboardName);
        
        asyncActionsExecutor.execute(getRibDashboardRaceInfoAction, new AsyncCallback<RibDashboardRaceInfoDTO>() {
            @Override
            public void onSuccess(RibDashboardRaceInfoDTO result) {
                switch (result.responseMessage) {
                case RACE_LIVE:
                    popupRacingNotYetStarted.hide(true);
                    List<RegattaAndRaceIdentifier> singletonList = Collections
                            .singletonList(result.idOfLastTrackedRace);
                    setSelection(singletonList);
                    if (result.competitorNamesFromLastTrackedRace != null
                            && result.competitorNamesFromLastTrackedRace.size() > 0
                            && !competitorSelectionPopup.isShown()) {
//                        competitorSelectionPopup.setCompetitorList(null);
//                        competitorSelectionPopup.addListener(RibDashboardDataRetriever.this);
                    }
                    notifyDataObservers(result);
                    break;

                case NO_RACE_LIVE:
                    if (numberOfCachedStartAnalysisDTOs == 0) {
                        popupRacingNotYetStarted.showWithMessageAndImageAndButtonText("Racing not yet started",
                                RibDashboardImageResources.INSTANCE.watch(), "Retry");
                    } else {
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
        /*GetRibDashboardRaceInfoAction getRibDashboardRaceInfoAction = new GetRibDashboardRaceInfoAction(ribDashboardService, leaderboardName);
         
        asyncActionsExecutor.execute(getRibDashboardRaceInfoAction, new AsyncCallback<RibDashboardRaceInfoDTO>() {
            @Override
            public void onSuccess(RibDashboardRaceInfoDTO result) {
                switch (result.responseMessage) {
                case OK:
                    popupRacingNotYetStarted.hide(true);
                    List<RegattaAndRaceIdentifier> singletonList = Collections
                            .singletonList(result.idOfLastTrackedRace);
                    setSelection(singletonList);
                    if (result.startAnalysisDTOList != null) {
                        int numberOfReceivedStartAnalysisDTOs = result.startAnalysisDTOList.size();
                        if (numberOfCachedStartAnalysisDTOs != numberOfReceivedStartAnalysisDTOs || shouldReloadStartAnalysis == true) {
                            shouldReloadStartAnalysis = false;
                            numberOfCachedStartAnalysisDTOs = numberOfReceivedStartAnalysisDTOs;
                            notifyNewStartAnalysisListener(result.startAnalysisDTOList, selectedCompetitorName);
                        }
                    }
                    if(competitorEditButton == null){
                        initCompetitorChangeButtonAndAddToRootPanel();
                        setCompetitorLabelText(selectedCompetitorName);
                    }
                    if (result.competitorNamesFromLastTrackedRace != null
                            && result.competitorNamesFromLastTrackedRace.size() > 0
                            && !competitorSelectionPopup.isShown()) {
                        competitorSelectionPopup.setCompetitorList(result.competitorNamesFromLastTrackedRace);
                        competitorSelectionPopup.addListener(RibDashboardDataRetriever.this);
                    }
                    notifyDataObservers(result);
                    break;
                case NO_COMPETITOR_SELECTED:
                    popupRacingNotYetStarted.hide(true);
                    if (result.competitorNamesFromLastTrackedRace != null
                            && result.competitorNamesFromLastTrackedRace.size() > 0
                            && !competitorSelectionPopup.isShown()) {
                        competitorSelectionPopup.setCompetitorList(result.competitorNamesFromLastTrackedRace);
                        competitorSelectionPopup.addListener(RibDashboardDataRetriever.this);
                        competitorSelectionPopup.show();
                    }
                    break;
                case NO_RACE_LIVE:
                    if (numberOfCachedStartAnalysisDTOs == 0) {
                        popupRacingNotYetStarted.showWithMessageAndImageAndButtonText("Racing not yet started",
                                RibDashboardImageResources.INSTANCE.watch(), "Retry");
                    } else {
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
        });*/
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

    @Override
    public void popupButtonClicked() {
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
