package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.adminconsole.DeviceConfigurationDetailComposite.DeviceConfigurationCloneListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class DeviceConfigurationUserPanel extends DeviceConfigurationPanel {

    public DeviceConfigurationUserPanel(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter reporter) {
        super(sailingService, stringMessages, reporter);
    }
    
    @Override
    protected DeviceConfigurationListComposite createListComposite(SailingServiceAsync sailingService, SelectionProvider<DeviceConfigurationMatcherDTO> selectionProvider, 
            ErrorReporter errorReporter, StringMessages stringMessages) {
        return new DeviceConfigurationUserListComposite(sailingService, selectionProvider, errorReporter, stringMessages);
    }
    
    @Override
    protected DeviceConfigurationDetailComposite createDetailComposite(SailingServiceAsync sailingService,
            ErrorReporter errorReporter, StringMessages stringMessages, DeviceConfigurationCloneListener cloneListener) {
        return new DeviceConfigurationUserDetailComposite(sailingService, errorReporter, stringMessages, cloneListener);
    }
    
    @Override
    protected DataEntryDialog<DeviceConfigurationMatcherDTO> getCreateDialog(StringMessages stringMessages,
            Validator<DeviceConfigurationMatcherDTO> validator, DialogCallback<DeviceConfigurationMatcherDTO> callback) {
        return new DeviceConfigurationCreateSingleMatcherDialog(stringMessages, validator, callback);
    }

}
