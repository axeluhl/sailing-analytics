package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

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
     * Creates a new {@link DateAndTimeInput} instance using the provided {@link Accuracy accuracy} and initial
     * {@link Date date value}.
     * 
     * @param initialValue
     *            the {@link Date date value} to show by default
     * @param accuracy
     *            the {@link Accuracy accuracy} of the input
     * @return the newly created {@link DateAndTimeInput} instance
     * 
     * @see DateAndTimeInput#DateAndTimeInput(Accuracy)
     */
    protected DateAndTimeInput createDateTimeBox(Date initialValue, Accuracy accuracy) {
        final DateAndTimeInput result = new DateAndTimeInput(accuracy);
        result.setValue(initialValue);
        result.addValueChangeHandler(event -> validateAndUpdate());
        // TODO: Enable DateAndTimeInput for enter and escape linking
        // DialogUtils.linkEnterToButton(getOkButton(), result);
        // DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

    /**
     * Creates a new {@link DateInput} instance using the provided initial {@link Date date value}.
     * 
     * @param initialValue
     *            the {@link Date date value} to show by default
     * @return the newly created {@link DateAndTimeInput} instance
     * 
     * @see DateAndTimeInput#DateAndTimeInput(Accuracy)
     */
    protected DateInput createDateBox(Date initialValue) {
        final DateInput result = new DateInput();
        result.setValue(initialValue);
        result.addValueChangeHandler(event -> validateAndUpdate());
        // TODO: Enable DateAndTimeInput for enter and escape linking
        // DialogUtils.linkEnterToButton(getOkButton(), result);
        // DialogUtils.linkEscapeToButton(getCancelButton(), result);
        return result;
    }

}
