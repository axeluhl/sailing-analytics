package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class MarkPropertiesDeviceIdentifierEditDialog extends DataEntryDialog<DeviceIdentifierDTO> {

    private final StringMessages stringMessages;
    protected final TextBox deviceIdTextBox;

    public MarkPropertiesDeviceIdentifierEditDialog(final StringMessages stringMessages,
            DeviceIdentifierDTO deviceIdentifierToEdit, DialogCallback<DeviceIdentifierDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.markProperties() + ": "
                + stringMessages.setDeviceIdentifier(), null, stringMessages.ok(), stringMessages.cancel(),
                new Validator<DeviceIdentifierDTO>() {
                    @Override
                    public String getErrorMessage(DeviceIdentifierDTO valueToValidate) {
                        String result = null;
                        if (valueToValidate != null
                                && (valueToValidate.deviceId == null || valueToValidate.deviceId.isEmpty())) {
                            return stringMessages.pleaseEnterA(stringMessages.deviceId());
                        }
                        return result;
                    }
                }, /* animationEnabled */ true, callback);
        this.ensureDebugId("MarkPropertiesDeviceIdentifierEditDialog");
        this.stringMessages = stringMessages;
        this.deviceIdTextBox = createTextBox(null);
    }

    @Override
    protected DeviceIdentifierDTO getResult() {
        return new DeviceIdentifierDTO("smartphoneUUID", deviceIdTextBox.getValue());
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(1, 2);
        result.setWidget(0, 0, new Label(stringMessages.deviceId()));
        result.setWidget(0, 1, deviceIdTextBox);
        return result;
    }
}
