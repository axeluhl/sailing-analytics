package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesWithFleetsCreateDialog extends DataEntryDialog<SeriesDTO> {

    private StringMessages stringConstants;
    private SeriesDTO series;

    private TextBox nameEntryField;
    private CheckBox isMedalSeriesCheckbox;

    private List<TextBox> fleetNameEntryFields;
    private List<ListBox> fleetColorEntryFields;
    private List<IntegerBox> fleetOrderNoEntryFields;

    private Grid fleetsGrid;

    protected static class SeriesParameterValidator implements Validator<SeriesDTO> {

        private StringMessages stringConstants;
        private ArrayList<SeriesDTO> existingSeries;

        public SeriesParameterValidator(StringMessages stringConstants, Collection<SeriesDTO> existingSeries) {
            this.stringConstants = stringConstants;
            this.existingSeries = new ArrayList<SeriesDTO>(existingSeries);
        }

        @Override
        public String getErrorMessage(SeriesDTO seriesToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = seriesToValidate.name != null && seriesToValidate.name.length() > 0;

            boolean unique = true;
            for (SeriesDTO series : existingSeries) {
                if (series.name.equals(seriesToValidate.name)) {
                    unique = false;
                    break;
                }
            }

            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!unique) {
                errorMessage = stringConstants.seriesWithThisNameAlreadyExists();
            }

            if(errorMessage == null) {
                List<FleetDTO> fleetsToValidate = seriesToValidate.getFleets();
                int index = 0;
                boolean fleetNameNotEmpty = true;

                for (FleetDTO fleet : fleetsToValidate) {
                    fleetNameNotEmpty = fleet.name != null && fleet.name.length() > 0;
                    if(!fleetNameNotEmpty) {
                        break;
                    }
                    index++;
                }

                int index2 = 0;
                boolean fleetUnique = true;
                
                HashSet<String> setToFindDuplicates = new HashSet<String>();
                for (FleetDTO fleet: fleetsToValidate) {
                    if(!setToFindDuplicates.add(fleet.name)) {
                        fleetUnique = false;
                        break;
                    }
                    index2++;
                }
                
                if (!fleetNameNotEmpty) {
                    errorMessage = stringConstants.fleet() + " " + (index + 1) + ": " + stringConstants.pleaseEnterNonEmptyName();
                } else if (!fleetUnique) {
                    errorMessage = stringConstants.fleet() + " " + (index2 + 1) + ": " + stringConstants.fleetWithThisNameAlreadyExists();
                }
                
            }
            
            return errorMessage;
        }

    }

    public SeriesWithFleetsCreateDialog(Collection<SeriesDTO> existingSeries, StringMessages stringConstants,
            AsyncCallback<SeriesDTO> callback) {
        super(stringConstants.series(), null, stringConstants.ok(), stringConstants.cancel(),  
                new SeriesParameterValidator(stringConstants, existingSeries), callback);
        this.stringConstants = stringConstants;
        this.series = new SeriesDTO();

        nameEntryField = createTextBox(null);
        nameEntryField.setWidth("200px");

        isMedalSeriesCheckbox = createCheckbox(stringConstants.medalSeries());
        
        fleetNameEntryFields = new ArrayList<TextBox>();
        fleetColorEntryFields = new ArrayList<ListBox>();
        fleetOrderNoEntryFields = new ArrayList<IntegerBox>(); 

        fleetsGrid = new Grid(0, 0);
        
        // create at least one fleet
        createFleetNameWidget("Default");
        createFleetOrderNoWidget(0);
        createFleetColorWidget(null);
    }

    private Widget createFleetNameWidget(String defaultName) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setWidth("175px");
        fleetNameEntryFields.add(textBox);
        return textBox; 
    }

    private Widget createFleetColorWidget(Color defaultColor) {
        final ListBox listBox = createListBox(false);
        final int fleetIndex = fleetNameEntryFields.size(); 
        listBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                // set default order no of the selected color
                int selIndex = listBox.getSelectedIndex();
                IntegerBox orderNoBox = fleetOrderNoEntryFields.get(fleetIndex-1);
                if(selIndex == 0) {
                    orderNoBox.setValue(0);
                } else {
                    String value = listBox.getValue(selIndex);
                    for(FleetColors color: FleetColors.values()) {
                        if(color.name().equals(value)) {
                            orderNoBox.setValue(color.getDefaultOrderNo());
                            break;
                        }
                    }
                }
            }
        });
        
        listBox.addItem(stringConstants.noColor());
        for(FleetColors value: FleetColors.values())
            listBox.addItem(value.name());

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
        series.name = nameEntryField.getText();
        series.setMedal(isMedalSeriesCheckbox.getValue());

        List<FleetDTO> fleets = new ArrayList<FleetDTO>();
        int groupsCount = fleetNameEntryFields.size();
        for(int i = 0; i < groupsCount; i++) {
            FleetDTO fleetDTO = new FleetDTO();
            fleetDTO.name = fleetNameEntryFields.get(i).getValue();
            fleetDTO.setColor(getSelectedColor(fleetColorEntryFields.get(i)));
            int orderNo = -1;
            if(fleetOrderNoEntryFields.get(i).getValue() != null) {
                orderNo = fleetOrderNoEntryFields.get(i).getValue();
            }
            fleetDTO.setOrderNo(orderNo);
            fleets.add(fleetDTO);
        }
        
        series.setFleets(fleets);

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
        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0,  0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 1, isMedalSeriesCheckbox);
        
        panel.add(createHeadlineLabel(stringConstants.fleets()));
        panel.add(fleetsGrid);
        
        Button addGroupButton = new Button("Add fleet");
        addGroupButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                createFleetNameWidget(null);
                createFleetOrderNoWidget(0);
                createFleetColorWidget(null);
                updateFleetsGrid(panel);
            }
        });
        panel.add(addGroupButton);
        
        updateFleetsGrid(panel);
        
        return panel;
    }

    private void updateFleetsGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(fleetsGrid);
        parentPanel.remove(fleetsGrid);
        
        int fleetCount = fleetNameEntryFields.size();
        fleetsGrid = new Grid(fleetCount + 1, 3);
        fleetsGrid.setCellSpacing(4);

        fleetsGrid.setHTML(0, 0, stringConstants.name());
        fleetsGrid.setHTML(0, 1, stringConstants.no());
        fleetsGrid.setHTML(0, 2, stringConstants.color());

        for(int i = 0; i < fleetCount; i++) {
            fleetsGrid.setWidget(i+1, 0, fleetNameEntryFields.get(i));
            fleetsGrid.setWidget(i+1, 1, fleetOrderNoEntryFields.get(i));
            fleetsGrid.setWidget(i+1, 2, fleetColorEntryFields.get(i));
        }

        parentPanel.insert(fleetsGrid, widgetIndex);
    }
    
    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
