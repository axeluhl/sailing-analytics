package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.dashboards.gwt.client.dataretriever.RibDashboardDataRetriever;
import com.sap.sailing.dashboards.gwt.client.dataretriever.RibDashboardDataRetrieverListener;
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
public class StartLineAdvantageByGeometryComponent extends LiveAverageComponent implements RibDashboardDataRetrieverListener {

    private static StringMessages stringConstants = StringMessages.INSTANCE;
    private static final Logger logger = Logger.getLogger(StartLineAdvantageByGeometryComponent.class.getName());
    
    public StartLineAdvantageByGeometryComponent(RibDashboardDataRetriever ribDashboardDataRetriever) {
        super(stringConstants.dashboardStartlineAdvantageByGeometry(), "m");
        StartLineAdvantageComponentRessources.INSTANCE.css().ensureInjected();
        this.header.setInnerText(stringConstants.dashboardStartlineAdvantageByGeometry());
        this.header.addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_header());
        liveAveragePanel.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_liveAveragePanel());
        livePanel.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_livePanel());
        middleLine.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_middleLine());
        averagePanel.getElement().addClassName(StartLineAdvantageComponentRessources.INSTANCE.css().startLineAdvantageComponent_averagePanel());
        this.liveLabel.setInnerHTML(stringConstants.dashboardLiveWind());
        this.averageLabel.setInnerHTML(stringConstants.dashboardAverageWind()+"<br>"+stringConstants.dashboardAverageWindMinutes(15));
        ribDashboardDataRetriever.addDataObserver(this);
    }
    
    public void setStartLineAdvantageValues(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        if (liveRaceInfoDTO.startLineAdvantageDTO != null) {
            logger.log(Level.INFO, "Updating UI with  RibDashboardRaceInfoDTO.startLineAdvantageDTO");
            setLiveValue(NumberFormat.getFormat("#0.0").format(liveRaceInfoDTO.startLineAdvantageDTO.startLineAdvantage));
            setAverageValue(NumberFormat.getFormat("#0.0").format(liveRaceInfoDTO.startLineAdvantageDTO.average));
        } else {
            logger.log(Level.INFO, "RibDashboardRaceInfoDTO.startLineAdvantageDTO is null");
        }
    }

    @Override
    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        setStartLineAdvantageValues(liveRaceInfoDTO);
    }
}
