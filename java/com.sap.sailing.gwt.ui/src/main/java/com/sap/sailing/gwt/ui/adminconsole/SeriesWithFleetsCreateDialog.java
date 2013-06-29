package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.IntegerBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesWithFleetsCreateDialog extends DataEntryDialog<SeriesDTO> {

    private StringMessages stringMessages;
    private SeriesDTO series;

    private TextBox nameEntryField;
    private CheckBox isMedalSeriesCheckbox;
    private CheckBox startsWithZeroScoreCheckbox;
    private CheckBox firstColumnIsNonDiscardableCarryForwardCheckbox;
    private CheckBox useSeriesResultDiscardingThresholdsCheckbox;
    private DiscardThresholdBoxes discardThresholdBoxes;

    private List<TextBox> fleetNameEntryFields;
    private List<ListBox> fleetColorEntryFields;
    private List<IntegerBox> fleetOrderNoEntryFields;

    private Grid fleetsGrid;

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
        fleetNameEntryFields = new ArrayList<TextBox>();
        fleetColorEntryFields = new ArrayList<ListBox>();
        fleetOrderNoEntryFields = new ArrayList<IntegerBox>(); 
        fleetsGrid = new Grid(0, 0);
        // create at least one fleet
        addFleetWidget("Default", 0, null);
    }

    private Widget createFleetNameWidget(String defaultName) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setVisibleLength(40);
        textBox.setWidth("175px");
        fleetNameEntryFields.add(textBox);
        return textBox; 
    }

    private Widget createFleetColorWidget(Color defaultColor) {
        final ListBox listBox = createListBox(false);
        final int fleetIndex = fleetNameEntryFields.size()-1;
        listBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                // set default order no of the selected color
                int selIndex = listBox.getSelectedIndex();
                IntegerBox orderNoBox = fleetOrderNoEntryFields.get(fleetIndex);
                TextBox nameBox = fleetNameEntryFields.get(fleetIndex);
                if (selIndex == 0) {
                    orderNoBox.setValue(0);
                } else {
                    String value = listBox.getValue(selIndex);
                    final FleetColors color = FleetColors.valueOf(value);
                    if (color != null) {
                        orderNoBox.setValue(color.getDefaultOrderNo());
                        nameBox.setValue(""+color.name().charAt(0)+color.name().toLowerCase().substring(1));
                    }
                }
                validate();
            }
        });
        listBox.addItem(stringMessages.noColor());
        for(FleetColors value: FleetColors.values()) {
            listBox.addItem(value.name());
        }
        if(defaultColor == null) {
            listBox.setSelectedIndex(0);
        }
        fleetColorEntryFields.add(listBox);
        return listBox; 
    }

    private Widget createFleetOrderNoWidget(int defaultValue) {
        IntegerBox intBox = createIntegerBox(defaultValue, 3); 
        fleetOrderNoEntryFields.add(intBox);
        return intBox; 
    }

    @Override
    protected SeriesDTO getResult() {
        series.setName(nameEntryField.getText());
        series.setMedal(isMedalSeriesCheckbox.getValue());
        series.setStartsWithZeroScore(startsWithZeroScoreCheckbox.getValue());
        series.setFirstColumnIsNonDiscardableCarryForward(firstColumnIsNonDiscardableCarryForwardCheckbox.getValue());
        List<FleetDTO> fleets = new ArrayList<FleetDTO>();
        int fleetsCount = fleetNameEntryFields.size();
        for(int i = 0; i < fleetsCount; i++) {
            FleetDTO fleetDTO = new FleetDTO();
            fleetDTO.setName(fleetNameEntryFields.get(i).getValue());
            fleetDTO.setColor(getSelectedColor(fleetColorEntryFields.get(i)));
            int orderNo = -1;
            if(fleetOrderNoEntryFields.get(i).getValue() != null) {
                orderNo = fleetOrderNoEntryFields.get(i).getValue();
            }
            fleetDTO.setOrderNo(orderNo);
            fleets.add(fleetDTO);
        }
        series.setFleets(fleets);
        series.setDiscardThresholds(useSeriesResultDiscardingThresholdsCheckbox.getValue() ? discardThresholdBoxes.getDiscardThresholds() : null);
        return series;
    }

    private Color getSelectedColor(ListBox colorListBox) {
        Color result = null;
        int selIndex = colorListBox.getSelectedIndex();
        // the zero index represents the 'no color' option
        if(selIndex > 0) {
            String value = colorListBox.getValue(selIndex);
            for(FleetColors color: FleetColors.values()) {
                if(color.name().equals(value)) {
                    result = color.getColor();
                    break;
                }
            }
        }
        return result;
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
        panel.add(createHeadlineLabel(stringMessages.fleets()));
        panel.add(fleetsGrid);
        Button addFleetButton = new Button(stringMessages.addFleet());
        addFleetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addFleetWidget(null, 0, null);
                updateFleetsGrid(panel);
            }
        });
        panel.add(addFleetButton);
        updateFleetsGrid(panel);
        return panel;
    }
    
    private void addFleetWidget(String fleetName, int ordering, Color color) {
        createFleetNameWidget(fleetName);
        createFleetOrderNoWidget(ordering);
        createFleetColorWidget(color);
    }

    private void updateFleetsGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(fleetsGrid);
        parentPanel.remove(fleetsGrid);
        int fleetCount = fleetNameEntryFields.size();
        fleetsGrid = new Grid(fleetCount + 1, 3);
        fleetsGrid.setCellSpacing(4);
        fleetsGrid.setHTML(0, 0, stringMessages.color());
        fleetsGrid.setHTML(0, 1, stringMessages.name());
        fleetsGrid.setHTML(0, 2, stringMessages.rank());
        for(int i = 0; i < fleetCount; i++) {
            fleetsGrid.setWidget(i+1, 0, fleetColorEntryFields.get(i));
            fleetsGrid.setWidget(i+1, 1, fleetNameEntryFields.get(i));
            fleetsGrid.setWidget(i+1, 2, fleetOrderNoEntryFields.get(i));
        }
        parentPanel.insert(fleetsGrid, widgetIndex);
    }
    
    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
