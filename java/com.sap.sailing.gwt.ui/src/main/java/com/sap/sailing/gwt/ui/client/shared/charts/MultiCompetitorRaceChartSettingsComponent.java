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

public class MultiCompetitorRaceChartSettingsComponent extends
        AbstractChartSettingsComponent<MultiCompetitorRaceChartSettings> implements
        SettingsDialogComponent<MultiCompetitorRaceChartSettings> {
    private ListBox chartFirstTypeSelectionListBox;
    private ListBox chartSecondTypeSelectionListBox;
    private final DetailType initialFirstDetailType;
    private final DetailType initialSecondDetailType;
    private final List<DetailType> availableDetailsTypes;

    public MultiCompetitorRaceChartSettingsComponent(MultiCompetitorRaceChartSettings settings,
            StringMessages stringMessages, boolean hasOverallLeaderboard) {
        super(settings, stringMessages);
        this.initialFirstDetailType = settings.getFirstDetailType();
        this.initialSecondDetailType = settings.getSecondDetailType();
        availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(DetailType.WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD);
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
        availableDetailsTypes.add(DetailType.RACE_CURRENT_RIDE_HEIGHT_IN_METERS);
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
        chartFirstTypeSelectionListBox = dialog.createListBox(/* isMultiSelect */false);
        int i = 0;
        for (DetailType detailType : availableDetailsTypes) {
            chartFirstTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialFirstDetailType) {
                chartFirstTypeSelectionListBox.setSelectedIndex(i);
            }
            i++;
        }
        mainPanel.add(chartFirstTypeSelectionListBox);
        chartSecondTypeSelectionListBox = dialog.createListBox(/* isMultiSelect */false);
        i = 0;
        for (DetailType detailType : availableDetailsTypes) {
            chartSecondTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialSecondDetailType) {
                chartSecondTypeSelectionListBox.setSelectedIndex(i);
            }
            i++;
        }
        chartSecondTypeSelectionListBox.addItem("--", "--");
        if (initialSecondDetailType == null) {
            chartSecondTypeSelectionListBox.setSelectedIndex(chartSecondTypeSelectionListBox.getItemCount() - 1);
        }
        mainPanel.add(chartSecondTypeSelectionListBox);
        mainPanel.add(new Label(stringMessages.stepSizeInSeconds()));
        stepSizeBox = dialog.createDoubleBox(((double) getSettings().getStepSize()) / 1000, 5);
        mainPanel.add(stepSizeBox);
        return mainPanel;
    }

    @Override
    public MultiCompetitorRaceChartSettings getResult() {
        DetailType newFirstDetailType = findSelectedTypeFor(chartFirstTypeSelectionListBox);
        DetailType newSecondDetailType = findSelectedTypeFor(chartSecondTypeSelectionListBox);
        if (com.sap.sse.common.Util.equalsWithNull(newFirstDetailType, newSecondDetailType)) {
            newSecondDetailType = null;
        }
        return new MultiCompetitorRaceChartSettings(getAbstractResult(), newFirstDetailType, newSecondDetailType);
    }

    private DetailType findSelectedTypeFor(ListBox typeSelectionListBox) {
        int itemIndex = typeSelectionListBox.getSelectedIndex();
        String selectedDetailType = typeSelectionListBox.getValue(itemIndex);
        for (DetailType detailType : availableDetailsTypes) {
            if (detailType.name().equals(selectedDetailType)) {
                return detailType;
            }
        }
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return chartFirstTypeSelectionListBox;
    }
}
