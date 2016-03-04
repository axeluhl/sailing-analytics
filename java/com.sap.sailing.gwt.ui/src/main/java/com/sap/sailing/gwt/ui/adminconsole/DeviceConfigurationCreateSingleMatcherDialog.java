package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class DeviceConfigurationCreateSingleMatcherDialog extends DataEntryDialog<DeviceConfigurationMatcherDTO> {
    
    public static class MatcherValidator implements Validator<DeviceConfigurationMatcherDTO> {
        
        private List<DeviceConfigurationMatcherDTO> allMatchers;
        private final StringMessages stringMessages;
    
        public MatcherValidator(List<DeviceConfigurationMatcherDTO> allMatchers, StringMessages stringMessages) {
            this.allMatchers = allMatchers;
            this.stringMessages = stringMessages;
        }
    
        @Override
        public String getErrorMessage(DeviceConfigurationMatcherDTO valueToValidate) {
            for (DeviceConfigurationMatcherDTO existingMatcher : allMatchers) {
                if (existingMatcher.clients.containsAll(valueToValidate.clients)) {
                    return stringMessages.thereIsAlreadyAConfigurationForThisDevice();
                }
            }
            for (String identifier : valueToValidate.clients) {
                if (identifier.isEmpty()) {
                    return stringMessages.enterDeviceIdentifierName();
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
    protected FocusWidget getInitialFocusWidget() {
        return identifierBox;
    }

    @Override
    protected DeviceConfigurationMatcherDTO getResult() {
        DeviceConfigurationMatcherDTO matcher = new DeviceConfigurationMatcherDTO();
        matcher.clients = Arrays.asList(identifierBox.getValue());
        return matcher;
    }
}
