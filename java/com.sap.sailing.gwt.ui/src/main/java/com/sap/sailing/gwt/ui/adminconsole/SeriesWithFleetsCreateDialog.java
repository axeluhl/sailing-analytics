package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.listedit.ListEditorComposite;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.ui.DataEntryDialog;

public class SeriesWithFleetsCreateDialog extends DataEntryDialog<SeriesDTO> {
    
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private StringMessages stringMessages;
    private SeriesDTO series;

    private TextBox nameEntryField;
    private CheckBox isMedalSeriesCheckbox;
    private CheckBox startsWithZeroScoreCheckbox;
    private CheckBox firstColumnIsNonDiscardableCarryForwardCheckbox;
    private CheckBox useSeriesResultDiscardingThresholdsCheckbox;
    private DiscardThresholdBoxes discardThresholdBoxes;
    private ListEditorComposite<FleetDTO> fleetListComposite;

    protected static class SeriesParameterValidator implements Validator<SeriesDTO> {
        private StringMessages stringMessages;
        private ArrayList<SeriesDTO> existingSeries;

        public SeriesParameterValidator(StringMessages stringConstants, Collection<SeriesDTO> existingSeries) {
            this.stringMessages = stringConstants;
            this.existingSeries = new ArrayList<SeriesDTO>(existingSeries);
        }

        @Override
        public String getErrorMessage(SeriesDTO seriesToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = seriesToValidate.getName() != null && seriesToValidate.getName().length() > 0;
            boolean unique = true;
            for (SeriesDTO series : existingSeries) {
                if (series.getName().equals(seriesToValidate.getName())) {
                    unique = false;
                    break;
                }
            }
            if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (!unique) {
                errorMessage = stringMessages.seriesWithThisNameAlreadyExists();
            }
            if (errorMessage == null) {
                List<FleetDTO> fleetsToValidate = seriesToValidate.getFleets();
                int index = 0;
                boolean fleetNameNotEmpty = true;
                for (FleetDTO fleet : fleetsToValidate) {
                    fleetNameNotEmpty = fleet.getName() != null && fleet.getName().length() > 0;
                    if (!fleetNameNotEmpty) {
                        break;
                    }
                    index++;
                }
                int index2 = 0;
                boolean fleetUnique = true;
                HashSet<String> setToFindDuplicates = new HashSet<String>();
                for (FleetDTO fleet: fleetsToValidate) {
                    if(!setToFindDuplicates.add(fleet.getName())) {
                        fleetUnique = false;
                        break;
                    }
                    index2++;
                }
                if (!fleetNameNotEmpty) {
                    errorMessage = stringMessages.fleet() + " " + (index + 1) + ": " + stringMessages.pleaseEnterAName();
                } else if (!fleetUnique) {
                    errorMessage = stringMessages.fleet() + " " + (index2 + 1) + ": " + stringMessages.fleetWithThisNameAlreadyExists();
                } else {
                    errorMessage = DiscardThresholdBoxes.getErrorMessage(seriesToValidate.getDiscardThresholds(), stringMessages);
                }
            }
            return errorMessage;
        }
    }

    public SeriesWithFleetsCreateDialog(Collection<SeriesDTO> existingSeries, StringMessages stringMessages,
            DialogCallback<SeriesDTO> callback) {
        super(stringMessages.series(), null, stringMessages.ok(), stringMessages.cancel(),  
                new SeriesParameterValidator(stringMessages, existingSeries), callback);
        this.stringMessages = stringMessages;
        this.series = new SeriesDTO();
        nameEntryField = createTextBox(null);
        nameEntryField.setVisibleLength(40);
        isMedalSeriesCheckbox = createCheckbox(stringMessages.medalSeries());
        startsWithZeroScoreCheckbox = createCheckbox(stringMessages.startsWithZeroScore());
        firstColumnIsNonDiscardableCarryForwardCheckbox = createCheckbox(stringMessages.firstRaceIsNonDiscardableCarryForward());
        useSeriesResultDiscardingThresholdsCheckbox = createCheckbox(stringMessages.seriesDefinesResultDiscardingRule());
        useSeriesResultDiscardingThresholdsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                discardThresholdBoxes.getWidget().setVisible(event.getValue());
            }
        });
        discardThresholdBoxes = new DiscardThresholdBoxes(this, stringMessages);
        discardThresholdBoxes.getWidget().setVisible(false);
        fleetListComposite = new FleetListEditorComposite(Arrays.asList(new FleetDTO("Default", 0, null)), stringMessages, resources.removeIcon());
        fleetListComposite.addValueChangeHandler(new ValueChangeHandler<List<FleetDTO>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<FleetDTO>> event) {
                validate();
            }
        });
    }

    @Override
    protected SeriesDTO getResult() {
        series.setName(nameEntryField.getText());
        series.setMedal(isMedalSeriesCheckbox.getValue());
        series.setStartsWithZeroScore(startsWithZeroScoreCheckbox.getValue());
        series.setFirstColumnIsNonDiscardableCarryForward(firstColumnIsNonDiscardableCarryForwardCheckbox.getValue());
        series.setFleets(fleetListComposite.getValue());
        series.setDiscardThresholds(useSeriesResultDiscardingThresholdsCheckbox.getValue() ? discardThresholdBoxes.getDiscardThresholds() : null);
        return series;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        Grid formGrid = new Grid(6, 2);
        panel.add(formGrid);
        formGrid.setWidget(0,  0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 1, isMedalSeriesCheckbox);
        formGrid.setWidget(2, 1, startsWithZeroScoreCheckbox);
        formGrid.setWidget(3, 1, firstColumnIsNonDiscardableCarryForwardCheckbox);
        formGrid.setWidget(4, 1, useSeriesResultDiscardingThresholdsCheckbox);
        formGrid.setWidget(5, 1, discardThresholdBoxes.getWidget());
        
        TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(fleetListComposite, stringMessages.fleets());
        tabPanel.selectTab(0);
        panel.add(tabPanel);
        return panel;
    }
    
    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
