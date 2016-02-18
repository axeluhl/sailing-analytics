package com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.wind;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartlineAdvantagesByWindAction;
import com.sap.sailing.dashboards.gwt.client.widgets.PollsLiveDataEvery5Seconds;
import com.sap.sailing.dashboards.gwt.client.widgets.header.DashboardWidgetHeaderAndNoDataMessage;
import com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.util.LiveAverageComponent;
import com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.wind.charts.StartlineAdvantagesOnLineChart;
import com.sap.sailing.dashboards.gwt.shared.DashboardURLParameters;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesByWindWidget extends Composite implements HasWidgets, PollsLiveDataEvery5Seconds {

    private static StartlineAdvantagesByWindComponentUiBinder uiBinder = GWT
            .create(StartlineAdvantagesByWindComponentUiBinder.class);

    interface StartlineAdvantagesByWindComponentUiBinder extends UiBinder<Widget, StartlineAdvantagesByWindWidget> {
    }
    
    interface StartlineAdvantagesByWindComponentStyle extends CssResource {
        
    }
    
    @UiField(provided = true)
    DashboardWidgetHeaderAndNoDataMessage dashboardWidgetHeaderAndNoDataMessage;
    
    @UiField(provided = true)
    LiveAverageComponent advantageMaximumLiveAverage;
    
    @UiField(provided = true)
    StartlineAdvantagesOnLineChart startlineAdvantagesOnLineChart;
    
    @UiField
    HTMLPanel dataDisplayContainer;
    
    public DashboardClientFactory dashboardClientFactory;
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesByWindWidget.class.getName());
    
    public StartlineAdvantagesByWindWidget(DashboardClientFactory dashboardClientFactory) {
        StartlineAdvantagesByWindWidgetResources.INSTANCE.gss().ensureInjected();
        this.dashboardClientFactory = dashboardClientFactory;
        dashboardWidgetHeaderAndNoDataMessage = new DashboardWidgetHeaderAndNoDataMessage();
        advantageMaximumLiveAverage = new LiveAverageComponent(StringMessages.INSTANCE.metersUnit());
        startlineAdvantagesOnLineChart = new StartlineAdvantagesOnLineChart();
        advantageMaximumLiveAverage.liveLabel.setInnerHTML("advantage max.");
        advantageMaximumLiveAverage.averageLabel.setInnerHTML("advantage max. average "+StringMessages.INSTANCE.dashboardAverageWindMinutes(15));
        initWidget(uiBinder.createAndBindUi(this));
        hideDataDisplayContainer();
        dashboardWidgetHeaderAndNoDataMessage.setHeaderText(StringMessages.INSTANCE.dashboardStartlineAdvantagesByWindHeader());
        dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByWindAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByWindAvailableMessage());
        dataDisplayContainer.getElement().getStyle().setOpacity(0.0);
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
                                dashboardWidgetHeaderAndNoDataMessage.hideNoDataMessage();
                                logger.log(Level.INFO, "Updating UI with StartlineAdvantagesWithMaxAndAverageDTO");
                                startlineAdvantagesOnLineChart.setStartlineAdvantagesAndConfidences(result.distanceToRCBoatToStartlineAdvantage, result.distanceToRCBoatToConfidence);
                                advantageMaximumLiveAverage.setLiveValue(NumberFormat.getFormat("#0.0").format(
                                        result.maximum.doubleValue()));
                                advantageMaximumLiveAverage.setAverageValue(NumberFormat.getFormat("#0.0").format(
                                        result.average.doubleValue()));
                                showDataDisplayContainer();
                            } else {
                                logger.log(Level.INFO, "StartlineAdvantagesWithMaxAndAverageDTO is null");
                                dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByWindAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByWindAvailableMessage());
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            logger.log(Level.INFO, "Failed to received StartlineAdvantagesWithMaxAndAverageDTO, "
                                    + caught.getMessage());
                            dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByWindAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByWindAvailableMessage());
                        }
                    });
        }
    }
    
    private void showDataDisplayContainer(){
        dataDisplayContainer.getElement().getStyle().setOpacity(1.0);
    }
    
    private void hideDataDisplayContainer(){
        dataDisplayContainer.getElement().getStyle().setOpacity(0.0);
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