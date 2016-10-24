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
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.GenericListBox;
import com.sap.sse.gwt.client.controls.GenericListBox.ValueBuilder;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SelectEventAndHostnameDialog extends DataEntryDialog<Pair<EventDTO, String>> {
    private GenericListBox<EventDTO> events;
    private TextBox serverUrl;
    private StringMessages stringMessages;
    private String leaderboardName;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;

    public SelectEventAndHostnameDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName,
            DialogCallback<Pair<EventDTO, String>> callback) {
        super(stringMessages.selectEventForInvitation(), null, stringMessages.inviteCompetitors(), stringMessages
                .cancel(), new Validator<Pair<EventDTO, String>>() {
            @Override
            public String getErrorMessage(Pair<EventDTO, String> valueToValidate) {
                if (valueToValidate.getA() == null) {
                    return stringMessages.pleaseSelectAnEvent();
                } else if (valueToValidate.getB() == null || valueToValidate.getB().isEmpty()) {
                    return stringMessages.pleaseEnterA(stringMessages.hostname());
                }
                return null;
            }
        }, false, callback);

        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
    }

    @Override
    protected Pair<EventDTO, String> getResult() {
        EventDTO event = events.getValue();
        String serverUrlString = serverUrl.getValue();
        if (serverUrlString.endsWith("/")) {
            serverUrlString = serverUrlString.substring(0, serverUrlString.length() - 1);
        }
        return new Pair<>(event, serverUrlString);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, createLabel(stringMessages.hostname()));
        serverUrl = createTextBox(Window.Location.getProtocol() + "//" + Window.Location.getHost());
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
        return grid;
    }
}