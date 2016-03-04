package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationDetailComposite.DeviceConfigurationCloneListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.security.ui.client.UserService;

public class DeviceConfigurationUserPanel extends DeviceConfigurationPanel {
    public DeviceConfigurationUserPanel(SailingServiceAsync sailingService, UserService userService,
            StringMessages stringMessages, ErrorReporter reporter) {
        super(sailingService, userService, stringMessages, reporter);
    }
    
    @Override
    protected DeviceConfigurationListComposite createListComposite(SailingServiceAsync sailingService, 
            ErrorReporter errorReporter, StringMessages stringMessages) {
        return new DeviceConfigurationUserListComposite(sailingService, errorReporter, stringMessages);
    }
    
    @Override
    protected DeviceConfigurationDetailComposite createDetailComposite(SailingServiceAsync sailingService,
            ErrorReporter errorReporter, StringMessages stringMessages, DeviceConfigurationCloneListener cloneListener) {
        return new DeviceConfigurationUserDetailComposite(sailingService, getUserService(), errorReporter, stringMessages, cloneListener);
    }
    
    @Override
    protected DataEntryDialog<DeviceConfigurationMatcherDTO> getCreateDialog(StringMessages stringMessages,
            Validator<DeviceConfigurationMatcherDTO> validator, DialogCallback<DeviceConfigurationMatcherDTO> callback) {
        return new DeviceConfigurationCreateSingleMatcherDialog(stringMessages, validator, callback);
    }

}
