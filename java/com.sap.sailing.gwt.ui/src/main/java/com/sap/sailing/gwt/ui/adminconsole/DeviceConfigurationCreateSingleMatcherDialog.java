package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.ui.DataEntryDialog;

public class DeviceConfigurationCreateSingleMatcherDialog extends DataEntryDialog<DeviceConfigurationMatcherDTO> {
    
    private TextBox identifierBox;
    
    public DeviceConfigurationCreateSingleMatcherDialog(StringMessages stringMessages,
            Validator<DeviceConfigurationMatcherDTO> validator,
            com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback<DeviceConfigurationMatcherDTO> callback) {
        super("Create Device Configuration", "Specify for which device the new configuration should apply", 
                "Create", stringMessages.cancel(), validator, callback);
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
