package com.sap.sailing.gwt.ui.shared.charts;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class MultiChartSettingsComponent extends AbstractChartSettingsComponent<MultiChartSettings> implements
        SettingsDialogComponent<MultiChartSettings> {
    private ListBox dataSelection;
    private final DetailType initialDataToShow;
    
    public MultiChartSettingsComponent(MultiChartSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
        this.initialDataToShow = settings.getDataToShow();
    }

    @Override
    public VerticalPanel getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel configPanel = super.getAdditionalWidget(dialog);
        configPanel.setSpacing(5);
        Label lblChart = new Label(getStringMessages().chooseChart());
        configPanel.add(lblChart);
        dataSelection = dialog.createListBox(/* isMultiSelect */ false);
        int i=0;
        for (DetailType detailType : new DetailType[] { DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER, DetailType.DISTANCE_TRAVELED,
                DetailType.VELOCITY_MADE_GOOD_IN_KNOTS, DetailType.GAP_TO_LEADER_IN_SECONDS, DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.RACE_RANK }) {
            dataSelection.addItem(DetailTypeFormatter.format(detailType, getStringMessages()), detailType.toString());
            if (detailType == initialDataToShow) {
                dataSelection.setSelectedIndex(i);
            }
            i++;
        }
        configPanel.add(dataSelection);
        return configPanel;
    }

    @Override
    public MultiChartSettings getResult() {
        DetailType dataToShow = null;
        for (DetailType dt : DetailType.values()){
            if (dt.toString().equals(dataSelection.getValue(dataSelection.getSelectedIndex()))){
                dataToShow = dt;
            }
        }
        return new MultiChartSettings(getAbstractResult(), dataToShow);
    }

}
