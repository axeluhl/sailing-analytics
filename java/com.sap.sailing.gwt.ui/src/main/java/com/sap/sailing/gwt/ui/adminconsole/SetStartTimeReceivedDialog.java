package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetStartTimeReceivedDialog extends DataEntryDialogWithDateTimeBox<Date> {

    private final StringMessages stringMessages;

    private DateAndTimeInput timeBox;

    public SetStartTimeReceivedDialog(StringMessages stringMessages, DataEntryDialog.DialogCallback<Date> callback) {
        super(stringMessages.setStartTimeReceived(), stringMessages.setStartTimeReceivedDescription(),
                stringMessages.ok(), stringMessages.cancel(), valueToValidate -> null, callback);
        this.stringMessages = stringMessages;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final FlowPanel panel = new FlowPanel();
        final Label noticeLabel = new Label(stringMessages.setStartTimeReceivedNotice());
        noticeLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        noticeLabel.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        panel.add(noticeLabel);
        final Grid content = new Grid(1, 3);
        final Label timeBoxLabel = new Label(stringMessages.startTime() + ":");
        content.setWidget(0, 0, timeBoxLabel);
        timeBox = createDateTimeBox(null, Accuracy.SECONDS);
        content.setWidget(0, 1, timeBox);
        final Button setNowButton = new Button(stringMessages.now());
        setNowButton.addClickHandler(event -> timeBox.setValue(new Date(), true));
        content.setWidget(0, 2, setNowButton);
        panel.add(content);
        return panel;
    }

    @Override
    protected Date getResult() {
        return timeBox.getValue();
    }
}
