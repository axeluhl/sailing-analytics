package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetTimePointDialog extends DataEntryDialogWithDateTimeBox<Date> {
    private final DateAndTimeInput time;
    private final StringMessages stringMessages;
    
    public SetTimePointDialog(final StringMessages stringMessages, String title, DialogCallback<Date> callback) {
        super(title, title, stringMessages.ok(), stringMessages.cancel(), new DataEntryDialog.Validator<Date>() {
            @Override
            public String getErrorMessage(Date valueToValidate) {
                if (valueToValidate == null) return stringMessages.pleaseEnterA(stringMessages.time());
                return null;
            }
        }, callback);
        
        this.stringMessages = stringMessages;
        
        time = createDateTimeBox(new Date(), Accuracy.SECONDS);
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(1, 2);
        grid.setWidget(0, 0, new Label(stringMessages.time()));
        grid.setWidget(0, 1, time);
        return grid;
    }
    
    @Override
    protected Date getResult() {
        return time.getValue();
    }
}
