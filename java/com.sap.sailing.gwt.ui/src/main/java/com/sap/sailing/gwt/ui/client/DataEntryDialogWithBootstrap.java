package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox.Format;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DialogUtils;

public abstract class DataEntryDialogWithBootstrap<T> extends DataEntryDialog<T> {

    public DataEntryDialogWithBootstrap(String title, String message, String okButtonName, String cancelButtonName,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator<T> validator, boolean animationEnabled,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<T> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, animationEnabled, callback);
    }

    protected DataEntryDialogWithBootstrap(String title, String message, String okButtonName, String cancelButtonName,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator<T> validator,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<T> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
    }
    
    /**
     * Call something like <code>setFormat("dd/mm/yyyy hh:ii")</code> on the result to set the date / time entry format.
     * @param yearToMinute 
     */
    public HTML5DateTimeBox createDateTimeBox(Date initialValue, Format format) {
        final HTML5DateTimeBox result = new HTML5DateTimeBox(format);
        result.setValue(initialValue);
        result.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                validateAndUpdate();
            }
        });
        DialogUtils.linkEnterToButton(getOkButton(), result);
        DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

}
