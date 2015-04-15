package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class MultiCompetitorRaceChartSettingsComponent extends AbstractChartSettingsComponent<MultiCompetitorRaceChartSettings> implements
        SettingsDialogComponent<MultiCompetitorRaceChartSettings> {
    private ListBox chartTypeSelectionListBox;
    private final DetailType initialDetailType;
    private final List<DetailType> availableDetailsTypes;    
    
    public MultiCompetitorRaceChartSettingsComponent(MultiCompetitorRaceChartSettings settings, StringMessages stringMessages, boolean hasOverallLeaderboard) {
        super(settings, stringMessages);
        this.initialDetailType = settings.getDetailType();
        
        availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER);
        availableDetailsTypes.add(DetailType.DISTANCE_TRAVELED);
        availableDetailsTypes.add(DetailType.DISTANCE_TRAVELED_INCLUDING_GATE_START);
        availableDetailsTypes.add(DetailType.VELOCITY_MADE_GOOD_IN_KNOTS);
        availableDetailsTypes.add(DetailType.GAP_TO_LEADER_IN_SECONDS);
        availableDetailsTypes.add(DetailType.RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS);
        availableDetailsTypes.add(DetailType.RACE_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.DISTANCE_TO_START_LINE);
        availableDetailsTypes.add(DetailType.BEAT_ANGLE);
        availableDetailsTypes.add(DetailType.COURSE_OVER_GROUND_TRUE_DEGREES);
        
        if (hasOverallLeaderboard) {
            availableDetailsTypes.add(DetailType.OVERALL_RANK);
        }
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(5);

        Label chartSelectionLabel = new Label(stringMessages.chooseChart());
        mainPanel.add(chartSelectionLabel);
        chartTypeSelectionListBox = dialog.createListBox(/* isMultiSelect */ false);
        int i=0;
        for (DetailType detailType : availableDetailsTypes) {
            chartTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialDetailType) {
                chartTypeSelectionListBox.setSelectedIndex(i);
            }
            i++;
        }
        mainPanel.add(chartTypeSelectionListBox);

        mainPanel.add(new Label(stringMessages.stepSizeInSeconds()));
        stepSizeBox = dialog.createDoubleBox(((double) getSettings().getStepSize()) / 1000, 5);
        mainPanel.add(stepSizeBox);
        
        return mainPanel;
    }

    @Override
    public MultiCompetitorRaceChartSettings getResult() {
        DetailType newDetailType = null;
        int selectedIndex = chartTypeSelectionListBox.getSelectedIndex();
        String selectedDetailType = chartTypeSelectionListBox.getValue(selectedIndex);
        for (DetailType detailType : availableDetailsTypes){
            if (detailType.name().equals(selectedDetailType)){
                newDetailType = detailType;
                break;
            }
        }
        return new MultiCompetitorRaceChartSettings(getAbstractResult(), newDetailType);
    }

    @Override
    public FocusWidget getFocusWidget() {
        return chartTypeSelectionListBox;
    }

}
