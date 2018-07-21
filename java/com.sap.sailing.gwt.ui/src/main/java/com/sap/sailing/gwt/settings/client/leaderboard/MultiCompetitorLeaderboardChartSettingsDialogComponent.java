package com.sap.sailing.gwt.settings.client.leaderboard;

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
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class MultiCompetitorLeaderboardChartSettingsDialogComponent implements SettingsDialogComponent<MultiCompetitorLeaderboardChartSettings> {
    private ListBox chartTypeSelectionListBox;
    private final DetailType initialDetailType;
    private final List<DetailType> availableDetailsTypes;
    private final boolean isOverall;

    public MultiCompetitorLeaderboardChartSettingsDialogComponent(MultiCompetitorLeaderboardChartSettings settings, boolean isOverall) {
        this.isOverall = isOverall;
        this.initialDetailType = settings.getDetailType();

        availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(MultiCompetitorLeaderboardChartSettings.getDefaultDetailType(isOverall));
        availableDetailsTypes.add(DetailType.REGATTA_NET_POINTS_SUM);
    }

    @Override
    public Validator<MultiCompetitorLeaderboardChartSettings> getValidator() {
        return new Validator<MultiCompetitorLeaderboardChartSettings>() {
            @Override
            public String getErrorMessage(MultiCompetitorLeaderboardChartSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(5);

        Label chartSelectionLabel = new Label(StringMessages.INSTANCE.chooseChart());
        mainPanel.add(chartSelectionLabel);
        chartTypeSelectionListBox = dialog.createListBox(/* isMultiSelect */false);
        chartTypeSelectionListBox.ensureDebugId("ChartTypeListBox");
        int i = 0;
        for (DetailType detailType : availableDetailsTypes) {
            chartTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialDetailType) {
                chartTypeSelectionListBox.setSelectedIndex(i);
            }
            i++;
        }
        mainPanel.add(chartTypeSelectionListBox);

        return mainPanel;
    }

    @Override
    public MultiCompetitorLeaderboardChartSettings getResult() {
        DetailType newDetailType = null;
        int selectedIndex = chartTypeSelectionListBox.getSelectedIndex();
        String selectedDetailType = chartTypeSelectionListBox.getValue(selectedIndex);
        for (DetailType detailType : availableDetailsTypes) {
            if (detailType.name().equals(selectedDetailType)) {
                newDetailType = detailType;
                break;
            }
        }
        return MultiCompetitorLeaderboardChartSettings.createWithDefaultDetailType(isOverall, newDetailType);
    }

    @Override
    public FocusWidget getFocusWidget() {
        return chartTypeSelectionListBox;
    }
}
