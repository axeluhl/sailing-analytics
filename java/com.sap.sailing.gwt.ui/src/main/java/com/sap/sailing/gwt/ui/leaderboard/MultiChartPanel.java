package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.server.api.RaceIdentifier;

/**
 * MultiChartPanel is a GWT panel that can show competitor data (e.g. current speed over ground, windward distance to
 * leader) for different races in a chart. The chart type can be selected from the settings.
 * 
 * When calling the consturcor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the sailing service returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if u know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author Benjamin Ebling (D056866)
 * 
 */
public class MultiChartPanel extends AbstractChartPanel<MultiChartSettings> implements Component<MultiChartSettings> {
    public MultiChartPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            RaceIdentifier[] races, final StringMessages stringMessages, int chartWidth, int chartHeight, ErrorReporter errorReporter) {
        super(sailingService, competitors, races, stringMessages, chartWidth, chartHeight, errorReporter);
    }

    @Override
    public SettingsDialogComponent<MultiChartSettings> getSettingsDialogComponent() {
        return new MultiChartSettingsComponent(new MultiChartSettings(getAbstractSettings(), getDataToShow()), getStringMessages());
    }

    @Override
    public void updateSettings(MultiChartSettings newSettings) {
    }

    @Override
    protected Component<MultiChartSettings> getComponent() {
        return this;
    }
}
