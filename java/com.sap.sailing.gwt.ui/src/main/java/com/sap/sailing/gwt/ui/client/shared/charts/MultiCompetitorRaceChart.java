package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * MultiCompetitorRaceChart is a GWT panel that can show competitor data (e.g. current speed over ground, windward distance to
 * leader) for different races in a chart. The chart type can be selected from the settings.
 * 
 * When calling the constructor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the SailingService returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if you know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author Benjamin Ebling (D056866), Axel Uhl (d043530)
 * 
 */
public class MultiCompetitorRaceChart extends AbstractCompetitorRaceChart<MultiCompetitorRaceChartSettings> implements Component<MultiCompetitorRaceChartSettings> {
    
    private final MultiCompetitorRaceChartLifecycle lifecycle;
    
    public MultiCompetitorRaceChart(Component<?> parent, ComponentContext<?> context,
            MultiCompetitorRaceChartLifecycle lifecycle,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, RegattaAndRaceIdentifier selectedRaceIdentifier,
            Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages,
            final ErrorReporter errorReporter, boolean compactChart, boolean allowTimeAdjust,
            final String leaderboardGroupName, String leaderboardName) {
        super(parent, context, sailingService, asyncActionsExecutor, competitorSelectionProvider,
                selectedRaceIdentifier, timer,
                timeRangeWithZoomProvider, stringMessages, errorReporter,
                /* show initially */DetailType.WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD, null, compactChart,
                allowTimeAdjust, leaderboardGroupName, leaderboardName);
        this.lifecycle = lifecycle;
    }
    
    @Override
    protected Button createSettingsButton() {
        Button settingsButton = SettingsDialog.createSettingsButton(this, stringMessages);
        return settingsButton;
    }

    @Override
    public MultiCompetitorRaceChartSettings getSettings() {
        return new MultiCompetitorRaceChartSettings(getAbstractSettings(), getSelectedFirstDetailType(), getSelectedSecondDetailType());
    }
    
    @Override
    public SettingsDialogComponent<MultiCompetitorRaceChartSettings> getSettingsDialogComponent(
            MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettingsComponent(settings, getStringMessages(),
                lifecycle.getAllowedDetailTypes());
    }

    @Override
    public void updateSettings(MultiCompetitorRaceChartSettings newSettings) {
        boolean settingsChanged = updateSettingsOnly(newSettings);
        boolean selectedDetailTypeChanged = setSelectedDetailTypes(newSettings.getFirstDetailType(),
                newSettings.getSecondDetailType());
        if (selectedDetailTypeChanged || settingsChanged) {
            clearChart();
            timeChanged(timer.getTime(), null);
        }
    }

    @Override
    protected Component<MultiCompetitorRaceChartSettings> getComponent() {
        return this;
    }

    @Override
    public String getLocalizedShortName() {
        return lifecycle.getLocalizedShortName();
    }

    @Override
    public String getDependentCssClassName() {
        return "multiCompetitorRaceChart";
    }

    @Override
    public String getId() {
        return lifecycle.getComponentId();
    }

}
