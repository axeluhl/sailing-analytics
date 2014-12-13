package com.sap.sailing.dashboards.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;

public class WindBotDataRetriever implements TimeListener, RaceSelectionChangeListener, WindBotDataRetrieverProvider {

    private AsyncActionsExecutor asyncActionsExecutor;
    private final SailingServiceAsync sailingService;
    private final String RIBDASHBOARD_WEB_CONTEXT_PATH = "dashboards";
    private RegattaAndRaceIdentifier currentLiveRace;
    private List<NumberOfWindBotsChangeListener> numberOfWindBotsChangeListeners;
    private List<WindBotDataRetrieverListener> windBotDataRetrieverListeners;
    private List<String> windBotIDsInLiveRace;

    private static final int WIND_CHART_RESOLUTION_IN_MILLISECONDS = 5000;
    public static final String LODA_WIND_CHART_DATA_CATEGORY = "loadWindChartData";
    private boolean didInitialLoading;

    public WindBotDataRetriever() {
        currentLiveRace = null;
        didInitialLoading = false;
        sailingService = GWT.create(SailingService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RIBDASHBOARD_WEB_CONTEXT_PATH,
                RemoteServiceMappingConstants.sailingServiceRemotePath);
        asyncActionsExecutor = new AsyncActionsExecutor();
        numberOfWindBotsChangeListeners = new ArrayList<NumberOfWindBotsChangeListener>();
        windBotDataRetrieverListeners = new ArrayList<WindBotDataRetrieverListener>(); 
        windBotIDsInLiveRace = new ArrayList<String>();
    }

    private void loadWindBotData(Date from, Date to, RegattaAndRaceIdentifier selectedRaceIdentifier) {
        if(!didInitialLoading){
            didInitialLoading = true;
            from = new Date(from.getTime()-1000*60*15);
        }
        GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, selectedRaceIdentifier, from, to,
                WIND_CHART_RESOLUTION_IN_MILLISECONDS, null, /*
                                                              * onlyUpToNewestEvent==true because we don't want to
                                                              * overshoot the evidence so far
                                                              */true);
        asyncActionsExecutor.execute(getWindInfoAction, LODA_WIND_CHART_DATA_CATEGORY,
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        if (result != null) {
                            if(windBotIDsInLiveRace.size() != getWindBotIdsFrom(result).size()){
                                windBotIDsInLiveRace = getWindBotIdsFrom(result);
                                notifyListenersAboutNumberOfWindBotChange(windBotIDsInLiveRace);
                            }
                            notifyWindBotDataRetrieverListeners(result);
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {

                    }
                });
    }

    public List<String> getWindBotIdsFrom(WindInfoForRaceDTO windInfoForRaceDTO) {
        List<String> windBotIds = new ArrayList<String>();
        for (WindSource windSource : windInfoForRaceDTO.windTrackInfoByWindSource.keySet()) {
            if (windSource.getType().equals(WindSourceType.EXPEDITION) && windSource.getId() != null) {
                windBotIds.add(windSource.getId().toString());
            }
        }
        return windBotIds;
    }

    public void addNumberOfWindBotsChangeListeners(NumberOfWindBotsChangeListener numberOfWindBotsChangeListener) {
        numberOfWindBotsChangeListeners.add(numberOfWindBotsChangeListener);
    }

    public void removeNumberOfWindBotsChangeListeners(NumberOfWindBotsChangeListener numberOfWindBotsChangeListener) {
        numberOfWindBotsChangeListeners.remove(numberOfWindBotsChangeListener);
    }

    private void notifyListenersAboutNumberOfWindBotChange(List<String> windBotIDs) {
        for (NumberOfWindBotsChangeListener numberOfWindBotsChangeListener : numberOfWindBotsChangeListeners) {
            numberOfWindBotsChangeListener.numberOfWindBotsChanged(windBotIDs, this);
        }
    }
    
    @Override
    public void addWindBotDataRetrieverListener(WindBotDataRetrieverListener windBotDataRetrieverListener) {
        windBotDataRetrieverListeners.add(windBotDataRetrieverListener);
    }
    @Override
    public void removeWindBotDataRetrieverListener(WindBotDataRetrieverListener windBotDataRetrieverListener) {
        windBotDataRetrieverListeners.remove(windBotDataRetrieverListener);
    }
    
    @Override
    public void notifyWindBotDataRetrieverListeners(WindInfoForRaceDTO windInfoForRaceDTO) {
        for (WindBotDataRetrieverListener windBotDataRetrieverListener : windBotDataRetrieverListeners) {
            windBotDataRetrieverListener.updateWindBotUI(windInfoForRaceDTO);
        }
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (currentLiveRace != null) {
            loadWindBotData(oldTime, newTime, currentLiveRace);
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces == null) {
            currentLiveRace = null;
        } else if (selectedRaces.size() > 0) {
            currentLiveRace = selectedRaces.get(0);
        }
    }
}
