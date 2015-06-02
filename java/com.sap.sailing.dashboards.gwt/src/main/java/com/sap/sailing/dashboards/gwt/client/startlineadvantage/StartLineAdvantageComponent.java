package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.dashboards.gwt.client.dataretriever.RibDashboardDataRetriever;
import com.sap.sailing.dashboards.gwt.client.dataretriever.RibDashboardDataRetrieverListener;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

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
public class StartLineAdvantageComponent extends LiveAverageComponent implements RibDashboardDataRetrieverListener {

    private StartLineAdvantageComponentState startLineAdvantageComponentState;
    private StartLineAdvantageComponentStateShowsAdvantageByWind startLineAdvantageComponentStateShowsAdvantageByWind;
    private StartLineAdvantageComponentStateShowsAdvantageByGeometry startLineAdvantageComponentStateShowsAdvantageByGeometry;
    private StringMessages stringConstants;
    
    public StartLineAdvantageComponent(RibDashboardDataRetriever ribDashboardDataRetriever) {
        super();
        stringConstants = StringMessages.INSTANCE;
        initAndSetStartLineAdvantageStates();
        StartLineAdvantageComponentRessources.INSTANCE.css().ensureInjected();
        this.header.setInnerText(stringConstants.dashboardStartlineAdvantageByWind());
        this.header.addClassName(StartLineAdvantageComponentRessources.INSTANCE.css()
                .startLineAdvantageComponent_header());
        liveAveragePanel.getElement().addClassName(
                StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_liveAveragePanel());
        livePanel.getElement().addClassName(
                StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_livePanel());
        middleLine.getElement().addClassName(
                StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_middleLine());
        averagePanel.getElement().addClassName(
                StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_averagePanel());
        this.liveLabel.setInnerHTML(stringConstants.dashboardLiveWind());
        this.averageLabel.setInnerHTML(stringConstants.dashboardAverageWind()+"<br>"+stringConstants.dashboardAverageWindMinutes(60));
        ribDashboardDataRetriever.addDataObserver(this);
    }

    private void initAndSetStartLineAdvantageStates() {
        startLineAdvantageComponentStateShowsAdvantageByWind = new StartLineAdvantageComponentStateShowsAdvantageByWind();
        startLineAdvantageComponentStateShowsAdvantageByGeometry = new StartLineAdvantageComponentStateShowsAdvantageByGeometry();
        startLineAdvantageComponentState = startLineAdvantageComponentStateShowsAdvantageByWind;
    }

    @Override
    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        if (liveRaceInfoDTO.startLineAdvantageDTO != null) {
            if(liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvatageType != null){
                setState(liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvatageType);
            }
            startLineAdvantageComponentState.setStartLineAdvantageValues(this, liveRaceInfoDTO);
        }
    }
    
    public void setState(StartlineAdvantageType startlineAdvantageType){
        switch (startlineAdvantageType) {
        case WIND:
            startLineAdvantageComponentState = startLineAdvantageComponentStateShowsAdvantageByWind;
            break;
        case GEOMETRIC:
            startLineAdvantageComponentState = startLineAdvantageComponentStateShowsAdvantageByGeometry;
            break;
        default:
            break;
        }
    }

    abstract class StartLineAdvantageComponentState {

        protected String lastLiveValue;
        protected String lastAverageValue;

        abstract void setStartLineAdvantageValues(StartLineAdvantageComponent statAdvantageComponent,
                RibDashboardRaceInfoDTO liveRaceInfoDTO);

        abstract void changeStartLineAdvatageComponentsStateToOtherState(
                StartLineAdvantageComponent startLineAdvantageComponent);

        public String getLastLiveValue() {
            return lastLiveValue;
        }

        public String getLastAverageValue() {
            return lastAverageValue;
        }
    }

    class StartLineAdvantageComponentStateShowsAdvantageByWind extends StartLineAdvantageComponentState {

        @Override
        public void setStartLineAdvantageValues(StartLineAdvantageComponent startLineAdvantageComponent,
                RibDashboardRaceInfoDTO liveRaceInfoDTO) {
            String liveValue = NumberFormat.getFormat("#0.0").format(
                    liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvantage);
            String averageValue = NumberFormat.getFormat("#0.0").format(
                    liveRaceInfoDTO.startLineAdvantageDTO.startlineAdvantageAverage);
            startLineAdvantageComponent.liveNumber.setInnerText(liveValue);
            startLineAdvantageComponent.averageNumber.setInnerText(averageValue);
            lastLiveValue = liveValue;
            lastAverageValue = averageValue;
            startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByGeometry.lastLiveValue = NumberFormat
                    .getFormat("#0.0").format(liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvantage);
            startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByGeometry.lastAverageValue = NumberFormat
                    .getFormat("#0.0").format(liveRaceInfoDTO.startLineAdvantageDTO.startlineAdvantageAverage);
        }

        @Override
        public void changeStartLineAdvatageComponentsStateToOtherState(
                StartLineAdvantageComponent startLineAdvantageComponent) {
            startLineAdvantageComponent.header.setInnerText(stringConstants.dashboardStartlineAdvantageByWind());
            startLineAdvantageComponent.startLineAdvantageComponentState = startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByGeometry;
            startLineAdvantageComponent.liveNumber
                    .setInnerText(startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByGeometry
                            .getLastLiveValue());
            startLineAdvantageComponent.averageNumber
                    .setInnerText(startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByGeometry
                            .getLastAverageValue());
        }
    }

    class StartLineAdvantageComponentStateShowsAdvantageByGeometry extends StartLineAdvantageComponentState {

        @Override
        public void setStartLineAdvantageValues(StartLineAdvantageComponent startLineAdvantageComponent,
                RibDashboardRaceInfoDTO liveRaceInfoDTO) {
            String liveValue = NumberFormat.getFormat("#0.0").format(
                    liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvantage);
            String averageValue = NumberFormat.getFormat("#0.0").format(
                    liveRaceInfoDTO.startLineAdvantageDTO.startlineAdvantageAverage);
            startLineAdvantageComponent.liveNumber.setInnerText(liveValue);
            startLineAdvantageComponent.averageNumber.setInnerText(averageValue);
            lastLiveValue = liveValue;
            lastAverageValue = averageValue;
            startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByWind.lastLiveValue = NumberFormat
                    .getFormat("#0.0").format(liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvantage);
            startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByWind.lastAverageValue = NumberFormat
                    .getFormat("#0.0").format(liveRaceInfoDTO.startLineAdvantageDTO.startlineAdvantageAverage);
        }

        @Override
        public void changeStartLineAdvatageComponentsStateToOtherState(
                StartLineAdvantageComponent startLineAdvantageComponent) {
            startLineAdvantageComponent.header.setInnerText(stringConstants.dashboardStartlineAdvantageByGeometry());
            startLineAdvantageComponent.startLineAdvantageComponentState = startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByWind;
            startLineAdvantageComponent.liveNumber
                    .setInnerText(startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByWind
                            .getLastLiveValue());
            startLineAdvantageComponent.averageNumber
                    .setInnerText(startLineAdvantageComponent.startLineAdvantageComponentStateShowsAdvantageByWind
                            .getLastAverageValue());
        }
    }
}
