package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode.ViewMode;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
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
     */
    public BetterDateTimeBox createDateTimeBox(Date initialValue) {
        final BetterDateTimeBox result = new BetterDateTimeBox();
        result.setValue(initialValue);
        result.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                validate();
            }
        });
        result.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    addAutoHidePartner(result.getPicker());
                }
            }
        });
        result.setAutoClose(true);
        result.setStartView(ViewMode.HOUR);
        DialogUtils.linkEnterToButton(getOkButton(), result.getBox());
        DialogUtils.linkEscapeToButton(getCancelButton(), result.getBox());
        return result;
    }

}
