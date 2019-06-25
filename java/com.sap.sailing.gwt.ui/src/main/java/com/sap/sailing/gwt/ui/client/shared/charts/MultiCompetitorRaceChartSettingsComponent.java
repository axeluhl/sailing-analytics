package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.DetailTypeComparator;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * This class is responsible for showing the settings dialog for the comptitorchart that is used for example in the
 * RaceBoard. It will depending on availableDetailTypes offer different options to select for the first and for the
 * second type. The availableDetailTypes are determined based on the environment of the corresponding race, for example
 * if additional foiling sensor data is imported. If non availableDetailTypes are selected for the first/second, instead
 * the dialog will show the first valid/none selected. Upon making to default the prior invalid values are overwritten,
 * they are kept otherwise.
 */
public class MultiCompetitorRaceChartSettingsComponent extends
        AbstractChartSettingsComponent<MultiCompetitorRaceChartSettings> implements
        SettingsDialogComponent<MultiCompetitorRaceChartSettings> {
    private ListBox chartFirstTypeSelectionListBox;
    private ListBox chartSecondTypeSelectionListBox;
    private final DetailType initialFirstDetailType;
    private final DetailType initialSecondDetailType;
    private final Iterable<DetailType> availableDetailsTypes;
    
    public MultiCompetitorRaceChartSettingsComponent(MultiCompetitorRaceChartSettings settings,
            StringMessages stringMessages, Iterable<DetailType> availableDetailTypes) {
        super(settings, stringMessages);
        this.initialFirstDetailType = settings.getFirstDetailType();
        this.initialSecondDetailType = settings.getSecondDetailType();
        this.availableDetailsTypes = availableDetailTypes;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(5);
        Label chartSelectionLabel = new Label(stringMessages.chooseChart());
        mainPanel.add(chartSelectionLabel);
        chartFirstTypeSelectionListBox = dialog.createListBox(/* isMultiSelect */false);
        chartSecondTypeSelectionListBox = dialog.createListBox(/* isMultiSelect */false);
        //add empty values, required, if a non available value is saved as default in the settings. Eg. rideheight, which is only valid for foiling races
        chartSecondTypeSelectionListBox.addItem("--", "--");
        int i = 0;
        
        List<DetailType> sortedAvailableDetailTypes = new ArrayList<DetailType>();
        Util.addAll(availableDetailsTypes, sortedAvailableDetailTypes);
        Collections.sort(sortedAvailableDetailTypes, new DetailTypeComparator());
        
        for (DetailType detailType : sortedAvailableDetailTypes) {
            chartFirstTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            chartSecondTypeSelectionListBox.addItem(DetailTypeFormatter.format(detailType), detailType.name());
            if (detailType == initialFirstDetailType) {
                chartFirstTypeSelectionListBox.setSelectedIndex(i);
            }
            if (detailType == initialSecondDetailType) {
                //add offset for empty item
                chartSecondTypeSelectionListBox.setSelectedIndex(i+1);
            }
            i++;
        }
        mainPanel.add(chartFirstTypeSelectionListBox);
        mainPanel.add(chartSecondTypeSelectionListBox);
        mainPanel.add(new Label(stringMessages.stepSizeInSeconds()));
        stepSizeBox = dialog.createDoubleBox(((double) getSettings().getStepSizeInMillis()) / 1000, 5);
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

    /**
     * Determines a selected DetailType of a Checkbox, returns null if not available 
     */
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
