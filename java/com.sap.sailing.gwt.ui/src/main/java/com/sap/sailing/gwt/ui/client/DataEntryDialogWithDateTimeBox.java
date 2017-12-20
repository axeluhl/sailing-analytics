package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.controls.datetime.TimeInput;
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
     *            the {@link Date date value} to show by default; <code>null</code> is permissible
     * @param accuracy
     *            the {@link Accuracy accuracy} of the input
     * @return the newly created {@link DateAndTimeInput} instance
     * 
     * @see DateAndTimeInput#DateAndTimeInput(Accuracy)
     */
    protected DateAndTimeInput createDateTimeBox(Date initialValue, Accuracy accuracy) {
        return createDateTimeInput(new DateAndTimeInput(accuracy), initialValue);
    }

    /**
     * Creates a new {@link DateInput} instance using the provided initial {@link Date date value}.
     * 
     * @param initialValue
     *            the {@link Date date value} to show by default; <code>null</code> is permissible
     * @return the newly created {@link DateInput} instance
     * 
     * @see DateInput#DateInput()
     */
    protected DateInput createDateBox(Date initialValue) {
        return createDateTimeInput(new DateInput(), initialValue);
    }

    /**
     * Creates a new {@link TimeInput} instance using the provided {@link Accuracy accuracy} and initial {@link Date
     * date value}.
     * 
     * @param initialValue
     *            the {@link Date date value} to show by default; <code>null</code> is permissible
     * @param accuracy
     *            the {@link Accuracy accuracy} of the input
     * @return the newly created {@link TimeInput} instance
     * 
     * @see TimeInput#TimeInput(Accuracy)
     */
    protected TimeInput createTimeBox(Date initialValue, Accuracy accuracy) {
        return createDateTimeInput(new TimeInput(accuracy), initialValue);
    }

    private <I extends DateTimeInput> I createDateTimeInput(I input, Date initialValue) {
        input.setValue(initialValue);
        input.addValueChangeHandler(event -> validateAndUpdate());
        // TODO: Enable DateTimeInputs for enter and escape linking
        // DialogUtils.linkEnterToButton(getOkButton(), input);
        // DialogUtils.linkEscapeToButton(getCancelButton(), input);
        return input;
    }

}
