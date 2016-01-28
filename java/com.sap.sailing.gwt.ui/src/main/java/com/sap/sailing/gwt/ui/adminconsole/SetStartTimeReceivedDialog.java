package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetStartTimeReceivedDialog extends DataEntryDialogWithBootstrap<Date> {

    private final StringMessages stringMessages;
    
    private BetterDateTimeBox timeBox;

    public SetStartTimeReceivedDialog(StringMessages stringMessages, DataEntryDialog.DialogCallback<Date> callback) {
        super(stringMessages.setStartTimeReceived(), stringMessages.setStartTimeReceivedDescription(), stringMessages.ok(), stringMessages.cancel(), new ReceivedStartTimeDialog(stringMessages), callback);
        this.stringMessages = stringMessages;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid content = new Grid(1, 2);
        
        Label timeBoxLabel = new Label(stringMessages.startTime() + ":");
        content.setWidget(0, 0, timeBoxLabel);
        timeBox = createDateTimeBox(new Date());
        content.setWidget(0, 1, timeBox);
        
        return content;
    }

    @Override
    protected Date getResult() {
        return timeBox.getValue();
    }
    
    private static class ReceivedStartTimeDialog implements Validator<Date> {

        private StringMessages stringMessages;

        public ReceivedStartTimeDialog(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(Date valueToValidate) {
            return valueToValidate == null ? stringMessages.pleaseEnterAValue() : null;
        }
        
    }

}
