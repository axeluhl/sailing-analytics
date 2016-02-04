package com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.course;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartlineAdvantageByGeometryAction;
import com.sap.sailing.dashboards.gwt.client.widgets.PollsLiveDataEvery5Seconds;
import com.sap.sailing.dashboards.gwt.client.widgets.header.DashboardWidgetHeaderAndNoDataMessage;
import com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.util.LiveAverageComponent;
import com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.wind.StartlineAdvantagesByWindWidget;
import com.sap.sailing.dashboards.gwt.shared.DashboardURLParameters;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantageByGeometryWidget extends Composite implements HasWidgets, PollsLiveDataEvery5Seconds {

    private static StartlineAdvantageByGeometryWidgetUiBinder uiBinder = GWT
            .create(StartlineAdvantageByGeometryWidgetUiBinder.class);

    interface StartlineAdvantageByGeometryWidgetUiBinder extends UiBinder<Widget, StartlineAdvantageByGeometryWidget> {
    }
    
    @UiField(provided = true)
    DashboardWidgetHeaderAndNoDataMessage dashboardWidgetHeaderAndNoDataMessage;
    
    @UiField(provided = true)
    LiveAverageComponent advantageMaximumLiveAverage;
    
    @UiField
    HTMLPanel contentContainer;

    public DashboardClientFactory dashboardClientFactory;
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesByWindWidget.class.getName());
    
    public StartlineAdvantageByGeometryWidget(DashboardClientFactory dashboardClientFactory) {
        StartlineAdvantageByGeometryWidgetRessources.INSTANCE.gss().ensureInjected();
        this.dashboardClientFactory = dashboardClientFactory;
        dashboardWidgetHeaderAndNoDataMessage = new DashboardWidgetHeaderAndNoDataMessage();
        advantageMaximumLiveAverage = new LiveAverageComponent(StringMessages.INSTANCE.metersUnit());
        advantageMaximumLiveAverage.liveLabel.setInnerHTML(StringMessages.INSTANCE.dashboardLiveWind());
        advantageMaximumLiveAverage.averageLabel.setInnerHTML("<p>"+StringMessages.INSTANCE.dashboardAverageWind()+"</p><p>"+StringMessages.INSTANCE.dashboardAverageWindMinutes(15)+"</p>");
        initWidget(uiBinder.createAndBindUi(this));
        hideDataDisplayContainer();
        dashboardWidgetHeaderAndNoDataMessage.setHeaderText(StringMessages.INSTANCE.dashboardStartlineAdvantageByGeometryHeader());
        registerForDashboardFiveSecondsTimer(dashboardClientFactory);
    }
    
    private void showDataDisplayContainer(){
        contentContainer.getElement().getStyle().setOpacity(1.0);
    }
    
    private void hideDataDisplayContainer(){
        contentContainer.getElement().getStyle().setOpacity(0.0);
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
                            hideDataDisplayContainer();
                            dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByGeometryAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByGeometryAvailableMessage());
                        }

                        @Override
                        public void onSuccess(StartLineAdvantageDTO result) {
                            if (result.startLineAdvantage != null) {
                                logger.log(Level.INFO, "Updating UI with  RibDashboardRaceInfoDTO.startLineAdvantageDTO");
                                advantageMaximumLiveAverage.setLiveValue(NumberFormat.getFormat("#0.0").format(result.startLineAdvantage.doubleValue()));
                                advantageMaximumLiveAverage.setAverageValue(NumberFormat.getFormat("#0.0").format(result.average.doubleValue()));
                                showDataDisplayContainer();
                                dashboardWidgetHeaderAndNoDataMessage.hideNoDataMessage();
                            } else {
                                logger.log(Level.INFO, "RibDashboardRaceInfoDTO.startLineAdvantageDTO is 0");
                                hideDataDisplayContainer();
                                dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByGeometryAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartlineAdvantagesByGeometryAvailableMessage());
                            }
                        }
                    });
        }
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