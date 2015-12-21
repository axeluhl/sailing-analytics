package com.sap.sailing.dashboards.gwt.client.dataretriever;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.dashboards.gwt.client.actions.GetRibDashboardRaceInfoAction;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
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
public class RibDashboardDataRetriever implements TimeListener {

    private String leaderboardName;
    private ArrayList<RibDashboardDataRetrieverListener> dataRetrieverListener;
    
    private AsyncActionsExecutor asyncActionsExecutor;
    
    private final RibDashboardServiceAsync ribDashboardService;

    private final Object MUTEX;

    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";
    private static final Logger logger = Logger.getLogger(RibDashboardDataRetriever.class.getName());

    public RibDashboardDataRetriever(RibDashboardServiceAsync ribDashboardService) {
        initNonFinalMemberVariablesWithNoArgumentConstructor();
        MUTEX = new Object();
        this.ribDashboardService = ribDashboardService;
        this.leaderboardName = Window.Location.getParameter(PARAM_LEADERBOARD_NAME);
        asyncActionsExecutor = new AsyncActionsExecutor();
    }

    private void initNonFinalMemberVariablesWithNoArgumentConstructor() {
        dataRetrieverListener = new ArrayList<RibDashboardDataRetrieverListener>();
    }

    private void loadLiveRaceInfoFromRibDashboadService() {
        
        GetRibDashboardRaceInfoAction getRibDashboardRaceInfoAction = new GetRibDashboardRaceInfoAction(ribDashboardService, leaderboardName);
        logger.log(Level.INFO, "Executing GetRibDashboardRaceInfoAction");
        asyncActionsExecutor.execute(getRibDashboardRaceInfoAction, new AsyncCallback<RibDashboardRaceInfoDTO>() {
            @Override
            public void onSuccess(RibDashboardRaceInfoDTO result) {
                logger.log(Level.INFO, "Received RibDashboardRaceInfoDTO");
                if (result != null) {
                switch (result.responseMessage) {
                case RACE_LIVE:
                    logger.log(Level.INFO, "RibDashboardRaceInfoDTO.responseMessage is RACE_LIVE");
                    if (result.idOfLastTrackedRace != null) {
                        logger.log(Level.INFO, "RibDashboardRaceInfoDTO idOfLastTrackedRace is "+result.idOfLastTrackedRace);
                    } else {
                        logger.log(Level.INFO, "RibDashboardRaceInfoDTO.idOfLastTrackedRace is null");
                    }
                    notifyDataObservers(result);
                    break;
                case NO_RACE_LIVE:
                    logger.log(Level.INFO, "RibDashboardRaceInfoDTO.responseMessage is NO_RACE_LIVE");
                    break;
                default:
                    logger.log(Level.INFO, "RibDashboardRaceInfoDTO.responseMessage is null");
                    break;
                }
                } else {
                    logger.log(Level.INFO, "RibDashboardRaceInfoDTO is null");
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.INFO, "Failed to received RibDashboardRaceInfoDTO, "+caught.getMessage());
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
        logger.log(Level.INFO, "Notifing RibDashboardDataRetrieverListener about new RibDashboardRaceInfoDTO");
        List<RibDashboardDataRetrieverListener> dataObserverCopy;
        synchronized (MUTEX) {
            dataObserverCopy = new ArrayList<RibDashboardDataRetrieverListener>(dataRetrieverListener);
        }
        for (RibDashboardDataRetrieverListener o : dataObserverCopy) {
            o.updateUIWithNewLiveRaceInfo(liveRaceInfoDTO);
        }
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadLiveRaceInfoFromRibDashboadService();
    }
}
