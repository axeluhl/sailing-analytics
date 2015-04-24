package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.GenericListBox;
import com.sap.sse.gwt.client.controls.GenericListBox.ValueBuilder;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class InviteBuoyTenderDialog extends DataEntryDialog<Triple<EventDTO, String, String>> {
    private GenericListBox<EventDTO> events;
    private TextBox serverUrl;
    private TextBox emails;
    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private String leaderboardName;
    private ErrorReporter errorReporter;

    public InviteBuoyTenderDialog(final StringMessages stringMessages, SailingServiceAsync sailingService, String leaderboardName, ErrorReporter errorReporter, DialogCallback<Triple<EventDTO, String, String>> callback) {
        super(stringMessages.selectEventForInvitation(), null, stringMessages.inviteCompetitors(), stringMessages
                .cancel(), new Validator<Triple<EventDTO, String, String>>() {
            @Override
            public String getErrorMessage(Triple<EventDTO, String, String> valueToValidate) {
                if (valueToValidate.getA() == null) {
                    return stringMessages.pleaseSelectAnEvent();
                } else if (valueToValidate.getB() == null || valueToValidate.getB().isEmpty()) {
                    return stringMessages.pleaseEnterA(stringMessages.hostname());
                } else if (valueToValidate.getC() == null || valueToValidate.getC().isEmpty()) {
                    return stringMessages.pleaseEnterA(stringMessages.email())+stringMessages.orMultipleEmails() ;
                }
                return null;
            }
        }, false, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.errorReporter = errorReporter;
    }

    @Override
    protected Triple<EventDTO, String, String> getResult() {
        EventDTO event = events.getValue();
        String serverUrlString = serverUrl.getValue();
        String emailsString = emails.getValue();
        if (serverUrlString.endsWith("/")) {
            serverUrlString = serverUrlString.substring(0, serverUrlString.length() - 1);
        }
        return new Triple<>(event, serverUrlString, emailsString);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(3, 3);
        grid.setWidget(0, 0, createLabel(stringMessages.hostname()));
        serverUrl = createTextBox(Window.Location.getProtocol() + "//" + Window.Location.getHost());
        emails = createTextBox("test@example.com,test2@example.com");
        grid.setWidget(0, 1, serverUrl);

        grid.setWidget(1, 0, createLabel(stringMessages.event()));
        events = createGenericListBox(new ValueBuilder<EventDTO>() {
            @Override
            public String getValue(EventDTO item) {
                if (item == null) {
                    return "";
                }
                return item.getName();
            }
        }, false);
        grid.setWidget(1, 1, events);

        events.addItem((EventDTO) null);
        sailingService.getEventsForLeaderboard(leaderboardName, new AsyncCallback<Collection<EventDTO>>() {
            @Override
            public void onSuccess(Collection<EventDTO> result) {
                events.addItems(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load events: " + caught.getMessage());
            }
        });
        
        grid.setWidget(2, 0, createLabel(stringMessages.email()));
        grid.setWidget(2, 1, emails);
        
        return grid;
    }
}
