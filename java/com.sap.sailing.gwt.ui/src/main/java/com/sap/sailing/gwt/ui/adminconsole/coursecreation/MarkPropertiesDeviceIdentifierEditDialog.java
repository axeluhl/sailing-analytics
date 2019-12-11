package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkPropertiesDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class MarkPropertiesDeviceIdentifierEditDialog extends DataEntryDialog<MarkPropertiesDTO> {

    private final MarkPropertiesDTO markPropertiesToEdit;
    private final StringMessages stringMessages;
    protected final TextBox deviceIdTextBox;

    public MarkPropertiesDeviceIdentifierEditDialog(final StringMessages stringMessages,
            MarkPropertiesDTO markPropertiesToEdit, DialogCallback<MarkPropertiesDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.markProperties() + ": "
                + stringMessages.setDeviceIdentifier(), null, stringMessages.ok(), stringMessages.cancel(),
                new Validator<MarkPropertiesDTO>() {
                    @Override
                    public String getErrorMessage(MarkPropertiesDTO valueToValidate) {
                        String result = null;
                        if (valueToValidate.getDeviceIdentifier() != null
                                && (valueToValidate.getDeviceIdentifier().deviceId == null
                                        || valueToValidate.getDeviceIdentifier().deviceId.isEmpty())) {
                            return stringMessages.pleaseEnterA(stringMessages.deviceId());
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("MarkPropertiesDeviceIdentifierEditDialog");
        this.stringMessages = stringMessages;
        this.markPropertiesToEdit = markPropertiesToEdit;
        this.deviceIdTextBox = createTextBox(null);
    }

    @Override
    protected MarkPropertiesDTO getResult() {
        return new MarkPropertiesDTO(markPropertiesToEdit.getUuid(), markPropertiesToEdit.getName(),
                markPropertiesToEdit.getTags(), new DeviceIdentifierDTO("smartphoneUUID", deviceIdTextBox.getValue()),
                /* position */ null, markPropertiesToEdit.getCommonMarkProperties().getShortName(),
                markPropertiesToEdit.getCommonMarkProperties().getColor(),
                markPropertiesToEdit.getCommonMarkProperties().getShape(),
                markPropertiesToEdit.getCommonMarkProperties().getPattern(),
                markPropertiesToEdit.getCommonMarkProperties().getType());
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(1, 2);
        result.setWidget(0, 0, new Label(stringMessages.deviceId()));
        result.setWidget(0, 1, deviceIdTextBox);
        return result;
    }
}
