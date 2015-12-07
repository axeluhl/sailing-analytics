package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartlineAdvantageByGeometryAction;
import com.sap.sailing.dashboards.gwt.shared.DashboardURLParameters;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * The purpose of the class is to display the live and average start line advantage either by wind or by geometry. There
 * is only one advantage display at once, so the user can switch between these following two
 * {@link StartLineAdvantageComponentState}s {@link StartLineAdvantageComponentStateShowsAdvantageByWind} and
 * {@link StartLineAdvantageComponentStateShowsAdvantageByGeometry} by tabing somewhere at the widget. The class extends
 * from {@link LiveAverageComponent} to get necessary Ui Elements but customizes these with own CSS.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class StartLineAdvantageByGeometryComponent extends LiveAverageComponent implements TimeListener {

    private DashboardClientFactory dashboardClientFactory;
    
    private static StringMessages stringConstants = StringMessages.INSTANCE;
    private static final Logger logger = Logger.getLogger(StartLineAdvantageByGeometryComponent.class.getName());
    
    public StartLineAdvantageByGeometryComponent(DashboardClientFactory dashboardClientFactory) {
        super(stringConstants.dashboardStartlineAdvantageByGeometry(), "m");
        this.dashboardClientFactory = dashboardClientFactory;
        StartLineAdvantageComponentRessources.INSTANCE.css().ensureInjected();
        this.header.setInnerText(stringConstants.dashboardStartlineAdvantageByGeometry());
        this.header.addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_header());
        liveAveragePanel.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_liveAveragePanel());
        livePanel.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_livePanel());
        middleLine.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_middleLine());
        averagePanel.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_averagePanel());
        this.liveLabel.setInnerHTML(stringConstants.dashboardLiveWind());
        this.averageLabel.setInnerHTML(stringConstants.dashboardAverageWind()+"<br>"+stringConstants.dashboardAverageWindMinutes(15));
        initSampleTimer();
    }
    
    private void initSampleTimer(){
        Timer timer = new Timer(PlayModes.Live);
        timer.setRefreshInterval(5000);
        timer.addTimeListener(this);
        timer.play();
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        String leaderboardNameParameterValue = DashboardURLParameters.LEADERBOARD_NAME.getValue();
        if (leaderboardNameParameterValue != null) {
            dashboardClientFactory.getDispatch().execute(
                    new GetStartlineAdvantageByGeometryAction(leaderboardNameParameterValue),
                    new AsyncCallback<StartLineAdvantageDTO>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            logger.log(Level.INFO, "Failed to received startline advantage by geometry");
                        }

                        @Override
                        public void onSuccess(StartLineAdvantageDTO result) {
                            if (result.startLineAdvantage != null) {
                                logger.log(Level.INFO, "Updating UI with  RibDashboardRaceInfoDTO.startLineAdvantageDTO");
                                setLiveValue(NumberFormat.getFormat("#0.0").format(result.startLineAdvantage.doubleValue()));
                                setAverageValue(NumberFormat.getFormat("#0.0").format(result.average.doubleValue()));
                            } else {
                                logger.log(Level.INFO, "RibDashboardRaceInfoDTO.startLineAdvantageDTO is 0");
                            }
                        }
                    });
        }
    }
}
