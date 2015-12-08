package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sailing.dashboards.gwt.client.PollsLiveDataEvery5Seconds;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartlineAdvantagesByWindAction;
import com.sap.sailing.dashboards.gwt.shared.DashboardURLParameters;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesByWindComponent extends Composite implements HasWidgets, PollsLiveDataEvery5Seconds {

    private static StartlineAdvantagesByWindComponentUiBinder uiBinder = GWT
            .create(StartlineAdvantagesByWindComponentUiBinder.class);

    interface StartlineAdvantagesByWindComponentUiBinder extends UiBinder<Widget, StartlineAdvantagesByWindComponent> {
    }
    
    interface StartlineAdvantagesByWindComponentStyle extends CssResource {
        
    }
    
    @UiField(provided = true)
    LiveAverageComponent advantageMaximumLiveAverage;
    
    @UiField(provided = true)
    StartlineAdvantagesOnLineChart startlineAdvantagesOnLineChart;
    
    @UiField
    StartlineAdvantagesByWindComponentStyle style;
    
    public DashboardClientFactory dashboardClientFactory;
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesByWindComponent.class.getName());
    
    public StartlineAdvantagesByWindComponent(DashboardClientFactory dashboardClientFactory) {
        this.dashboardClientFactory = dashboardClientFactory;
        startlineAdvantagesOnLineChart = new StartlineAdvantagesOnLineChart();
        advantageMaximumLiveAverage = new LiveAverageComponent(StringMessages.INSTANCE.dashboardStartlineAdvantagesByWind(), "m");
        advantageMaximumLiveAverage.header.getStyle().setFontSize(14, Unit.PT);
        advantageMaximumLiveAverage.liveLabel.setInnerHTML("advantage max.");
        advantageMaximumLiveAverage.averageLabel.setInnerHTML("advantage max. average "+StringMessages.INSTANCE.dashboardAverageWindMinutes(15));
        initWidget(uiBinder.createAndBindUi(this));
        registerForDashboardFiveSecondsTimer(dashboardClientFactory);
    }
    
    private void loadData() {
        logger.log(Level.INFO, "Executing GetStartlineAdvantagesAction");
        String leaderboardNameParameterValue = DashboardURLParameters.LEADERBOARD_NAME.getValue();
        if (leaderboardNameParameterValue != null) {
            dashboardClientFactory.getDispatch().execute(
                    new GetStartlineAdvantagesByWindAction(leaderboardNameParameterValue),
                    new AsyncCallback<StartlineAdvantagesWithMaxAndAverageDTO>() {

                        @Override
                        public void onSuccess(StartlineAdvantagesWithMaxAndAverageDTO result) {
                            logger.log(Level.INFO, "Received StartlineAdvantagesWithMaxAndAverageDTO");
                            if (result != null && result.maximum != null && result.average != null) {
                                logger.log(Level.INFO, "Updating UI with StartlineAdvantagesWithMaxAndAverageDTO");
                                startlineAdvantagesOnLineChart.setStartlineAdvantages(result.advantages);
                                advantageMaximumLiveAverage.setLiveValue(NumberFormat.getFormat("#0.0").format(
                                        result.maximum.doubleValue()));
                                advantageMaximumLiveAverage.setAverageValue(NumberFormat.getFormat("#0.0").format(
                                        result.average.doubleValue()));
                            } else {
                                logger.log(Level.INFO, "StartlineAdvantagesWithMaxAndAverageDTO is null");
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            logger.log(Level.INFO, "Failed to received StartlineAdvantagesWithMaxAndAverageDTO, "
                                    + caught.getMessage());
                        }
                    });
        }
    }
    
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadData();
    }
    
    @Override
    public void registerForDashboardFiveSecondsTimer(DashboardClientFactory dashboardClientFactory) {
        if (dashboardClientFactory != null) {
            dashboardClientFactory.getDashboardFiveSecondsTimer().addTimeListener(this);
        }
    }
    
    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}