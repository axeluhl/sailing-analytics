package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class DeviceConfigurationCreateMatcherDialog extends DataEntryDialog<DeviceConfigurationMatcherDTO> {
    
    public static class MatcherValidator implements Validator<DeviceConfigurationMatcherDTO> {
        
        private List<DeviceConfigurationMatcherDTO> allMatchers;

        public MatcherValidator(List<DeviceConfigurationMatcherDTO> allMatchers) {
            this.allMatchers = allMatchers;
        }

        @Override
        public String getErrorMessage(DeviceConfigurationMatcherDTO valueToValidate) {
            for (DeviceConfigurationMatcherDTO existingMatcher : allMatchers) {
                if (existingMatcher.type.equals(valueToValidate.type)) {
                    switch (valueToValidate.type) {
                    case SINGLE:
                        if (existingMatcher.clients.containsAll(valueToValidate.clients)) {
                            return "There is already a configuration for such a matcher.";
                        }
                        break;
                    case MULTI:
                        if (existingMatcher.clients.containsAll(valueToValidate.clients) &&
                                valueToValidate.clients.containsAll(existingMatcher.clients)) {
                            return "There is already a configuration for such a matcher.";
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            for (String identifier : valueToValidate.clients) {
                if (identifier.isEmpty()) {
                    return "Enter an identifier name";
                }
            }
            return null;
        }
        
    }

    private ListBox typeBox;
    private List<TextBox> devicesBoxes;
    
    public DeviceConfigurationCreateMatcherDialog(StringMessages stringMessages,
            Validator<DeviceConfigurationMatcherDTO> validator,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<DeviceConfigurationMatcherDTO> callback) {
        super("Create Device Configuration", "Specify for which devices the new configuration should apply", 
                "Create", stringMessages.cancel(), validator, callback);
        this.devicesBoxes = new ArrayList<TextBox>();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final Grid valueGrid = new Grid(2, 2);
        
        valueGrid.setWidget(0, 0, createLabel("Matcher type"));
        typeBox = createListBox(false);
        typeBox.addItem(DeviceConfigurationMatcherType.SINGLE.name());
        typeBox.addItem(DeviceConfigurationMatcherType.MULTI.name());
        valueGrid.setWidget(0, 1, typeBox);
        valueGrid.setWidget(1, 0, createLabel("Matching devices"));

        final Grid devicesGrid = new Grid(2, 2);
        TextBox firstDeviceBox = createTextBox("");
        devicesGrid.setWidget(0, 0, firstDeviceBox);
        devicesBoxes.add(firstDeviceBox);
        
        final Button addDeviceButton = new Button("Add Device");
        addDeviceButton.setVisible(false);
        addDeviceButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                devicesGrid.insertRow(0);
                TextBox deviceBox = createTextBox("");
                devicesBoxes.add(deviceBox);
                devicesGrid.setWidget(0, 0, deviceBox);
                devicesGrid.setWidget(0, 1, createRemoveButton(devicesGrid));
            }
        });
        devicesGrid.setWidget(1, 0, addDeviceButton);
        valueGrid.setWidget(1, 1, devicesGrid);
        
        typeBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                DeviceConfigurationMatcherType type = DeviceConfigurationMatcherType.valueOf(typeBox.getValue(typeBox.getSelectedIndex()));
                switch (type) {
                case SINGLE:
                    valueGrid.getWidget(1, 0).setVisible(true);
                    valueGrid.getWidget(1, 1).setVisible(true);
                    devicesGrid.getWidget(0, 0).setVisible(true);
                    devicesGrid.getWidget(1, 0).setVisible(false);
                    for (int i = 0; i < devicesGrid.getRowCount() - 2; i++) {
                        devicesGrid.getWidget(i, 0).setVisible(false);
                        devicesGrid.getWidget(i, 1).setVisible(false);
                    }
                    break;
                case MULTI:
                    valueGrid.getWidget(1, 0).setVisible(true);
                    valueGrid.getWidget(1, 1).setVisible(true);
                    devicesGrid.getWidget(0, 0).setVisible(true);
                    devicesGrid.getWidget(1, 0).setVisible(true);
                    for (int i = 0; i < devicesGrid.getRowCount() - 2; i++) {
                        devicesGrid.getWidget(i, 0).setVisible(true);
                        devicesGrid.getWidget(i, 1).setVisible(true);
                    }
                    break;
                default:
                    valueGrid.getWidget(1, 0).setVisible(false);
                    valueGrid.getWidget(1, 1).setVisible(false);
                    break;
                }
            }
        });
        
        return valueGrid;
    }

    @Override
    protected DeviceConfigurationMatcherDTO getResult() {
        DeviceConfigurationMatcherDTO dto = new DeviceConfigurationMatcherDTO();
        dto.type = DeviceConfigurationMatcherType.valueOf(typeBox.getValue(typeBox.getSelectedIndex()));
        switch (dto.type) {
        case SINGLE:
            dto.clients = Arrays.asList(devicesBoxes.get(0).getText());
            break;
        case MULTI:
            dto.clients = new ArrayList<String>();
            for (TextBox textBox : devicesBoxes) {
                dto.clients.add(textBox.getText());
            }
            break;        
        default:
            dto.clients = Collections.emptyList();
            break;
        }
        return dto;
    }

    private PushButton createRemoveButton(final Grid devicesGrid) {
        PushButton removeButton = new PushButton(new Image(IconResources.INSTANCE.removeIcon()));
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int rowToBeRemoved = devicesGrid.getCellForEvent(event).getRowIndex();
                devicesBoxes.remove(devicesGrid.getWidget(rowToBeRemoved, 0));
                devicesGrid.removeRow(rowToBeRemoved);
            }
        });
        return removeButton;
    }

}
