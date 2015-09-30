package com.sap.sailing.dashboards.gwt.client.dataretriever;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.dashboards.gwt.client.actions.GetIDFromRaceThatTakesWindFixesNowAction;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;

public class WindBotDataRetriever implements TimeListener, WindBotDataRetrieverProvider {

    private AsyncActionsExecutor asyncActionsExecutor;
    private final RibDashboardServiceAsync ribDashboardService;
    private final SailingServiceAsync sailingService;
    private List<NumberOfWindBotsChangeListener> numberOfWindBotsChangeListeners;
    private List<WindBotDataRetrieverListener> windBotDataRetrieverListeners;
    private List<String> windBotIDsInLiveRace;
    private String leaderboardName;

    private final Set<String> windSourceTypeNames;
    private final int WIND_CHART_RESOLUTION_IN_MILLISECONDS = 5000;
    private final int ONE_HOUR_IN_MILLISECONDS = 1000*60*60;
    private final String LODA_WIND_CHART_DATA_CATEGORY = "loadWindChartData";
    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";
    private boolean didInitialLoading;
    
    private static final Logger logger = Logger.getLogger(WindBotDataRetriever.class.getName());
    
    public WindBotDataRetriever(RibDashboardServiceAsync ribDashboardService, SailingServiceAsync sailingService) {
        this.ribDashboardService = ribDashboardService;
        this.sailingService = sailingService;
        didInitialLoading = false;
        asyncActionsExecutor = new AsyncActionsExecutor();
        numberOfWindBotsChangeListeners = new ArrayList<NumberOfWindBotsChangeListener>();
        windBotDataRetrieverListeners = new ArrayList<WindBotDataRetrieverListener>(); 
        windBotIDsInLiveRace = new ArrayList<String>();
        windSourceTypeNames = new HashSet<>();
        this.leaderboardName = Window.Location.getParameter(PARAM_LEADERBOARD_NAME);
        windSourceTypeNames.add(WindSourceType.EXPEDITION.name());
    }

    private void loadWindBotData(Date from, Date to, RegattaAndRaceIdentifier selectedRaceIdentifier) {
        if (!didInitialLoading) {
            didInitialLoading = true;
            from = new Date(from.getTime()-ONE_HOUR_IN_MILLISECONDS);
        }
        logger.log(Level.INFO, "Executing WindInfoAction with from "+from+" and to "+to);
        GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, selectedRaceIdentifier, from, to,
                WIND_CHART_RESOLUTION_IN_MILLISECONDS, windSourceTypeNames, /*
                                                              * onlyUpToNewestEvent==true because we don't want to
                                                              * overshoot the evidence so far
                                                              */true);
        asyncActionsExecutor.execute(getWindInfoAction, LODA_WIND_CHART_DATA_CATEGORY,
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        logger.log(Level.INFO, "Received Response from WindInfoAction");
                        if (result != null) {
                            if(windBotIDsInLiveRace.size() != getWindBotIdsFrom(result).size()){
                                windBotIDsInLiveRace = getWindBotIdsFrom(result);
                                notifyListenersAboutNumberOfWindBotChange(windBotIDsInLiveRace);
                                logger.log(Level.INFO, "Number of Windbots changed");
                            }
                            notifyWindBotDataRetrieverListeners(result);
                        } else {
                            logger.log(Level.INFO, "Response WindInfoForRaceDTO from WindInfoAction null");                            
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.INFO, caught.getMessage().toString());
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
        final Date finalNewTime = newTime;
        final Date finaloldTime = oldTime;
        GetIDFromRaceThatTakesWindFixesNowAction getIDFromRaceThatTakesWindFixesNowAction = new GetIDFromRaceThatTakesWindFixesNowAction(ribDashboardService, leaderboardName);
        asyncActionsExecutor.execute(getIDFromRaceThatTakesWindFixesNowAction, new AsyncCallback<RegattaAndRaceIdentifier>() {
            @Override
            public void onSuccess(RegattaAndRaceIdentifier result) {
                loadWindBotData(finaloldTime, finalNewTime, result);
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.INFO, caught.getMessage().toString());
            }
        });
    }
}
