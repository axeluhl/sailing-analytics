package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.gwt.common.client.datetime.DateAndTimeInput;
import com.sap.sailing.gwt.common.client.datetime.DateTimeInput.Accuracy;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox;
import com.sap.sailing.gwt.ui.shared.HTML5DateTimeBox.Format;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DialogUtils;

public abstract class DataEntryDialogWithDateTimeBox<T> extends DataEntryDialog<T> {

    protected DataEntryDialogWithDateTimeBox(String title, String message, String okButtonName, String cancelButtonName,
            Validator<T> validator, boolean animationEnabled, DialogCallback<T> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, animationEnabled, callback);
    }

    protected DataEntryDialogWithDateTimeBox(String title, String message, String okButtonName, String cancelButtonName,
            Validator<T> validator, DialogCallback<T> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
    }
    
    /**
     * Creates a new {@link HTML5DateTimeBox} instance, where the requested {@link Format format} will be honored by the
     * underlying implementation, if the native components can support it.
     * 
     * @param initialValue
     *            the initial {@link Date value} to set to the created {@link HTML5DateTimeBox}
     * @param format
     *            the {@link Format format} to set to the created {@link HTML5DateTimeBox}
     */
    protected HTML5DateTimeBox createDateTimeBox(Date initialValue, Format format) {
        final HTML5DateTimeBox result = new HTML5DateTimeBox(format);
        result.setValue(initialValue);
        result.addValueChangeHandler(event -> validateAndUpdate());
        DialogUtils.linkEnterToButton(getOkButton(), result);
        DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

    protected DateAndTimeInput createDateTimeBox(Date initialValue, Accuracy accuracy) {
        final DateAndTimeInput result = new DateAndTimeInput(accuracy);
        result.setValue(initialValue);
        result.addValueChangeHandler(event -> validateAndUpdate());
        // TODO: Enable DateAndTimeInput for enter and escape linking
        // DialogUtils.linkEnterToButton(getOkButton(), result);
        // DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

}
