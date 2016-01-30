package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class DeviceConfigurationCreateSingleMatcherDialog extends DataEntryDialog<DeviceConfigurationMatcherDTO> {
    
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

    private TextBox identifierBox;
    
    public DeviceConfigurationCreateSingleMatcherDialog(StringMessages stringMessages,
            Validator<DeviceConfigurationMatcherDTO> validator,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<DeviceConfigurationMatcherDTO> callback) {
        super(stringMessages.createDeviceConfiguration(), stringMessages.forWhichDeviceShouldConfigurationApply(), 
                stringMessages.create(), stringMessages.cancel(), validator, callback);
        this.identifierBox = createTextBox("");
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(1, 2);
        grid.setWidget(0, 0, createLabel("Identifier"));
        grid.setWidget(0, 1, identifierBox);
        return grid;
    }

    @Override
    protected DeviceConfigurationMatcherDTO getResult() {
        DeviceConfigurationMatcherDTO matcher = new DeviceConfigurationMatcherDTO();
        matcher.type = DeviceConfigurationMatcherType.SINGLE;
        matcher.clients = Arrays.asList(identifierBox.getValue());
        return matcher;
    }
}
