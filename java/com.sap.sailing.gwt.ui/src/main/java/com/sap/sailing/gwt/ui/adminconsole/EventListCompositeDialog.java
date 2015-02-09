package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.EventSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.EventSelectionModel;
import com.sap.sailing.gwt.ui.client.EventSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public class EventListCompositeDialog extends DataEntryDialogWithBootstrap<List<UUID>> implements EventSelectionChangeListener {
    protected StringMessages stringMessages;

    private EventSelectionProvider eventSelectionProvider;
    private EventListComposite eventListComposite;
    private List<UUID> selectedEvents;

    protected static class EventListValidator implements Validator<List<UUID>> {
        private StringMessages stringMessages;
        
        public EventListValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }
        
        @Override
        public String getErrorMessage(List<UUID> valueToValidate) {
            if (valueToValidate.isEmpty()){
                return stringMessages.pleaseSelectAnEvent();
            }
            return null;
        }
    }

    public EventListCompositeDialog(SailingServiceAsync sailingService, StringMessages stringMessages,final ErrorReporter errorReporter, DialogCallback<List<UUID>> callback) {
        super(stringMessages.event(), null, stringMessages.ok(), stringMessages.cancel(), new EventListValidator(stringMessages), callback);
        this.stringMessages = stringMessages;
        
        eventSelectionProvider = new EventSelectionModel(true);
        eventSelectionProvider.addEventSelectionChangeListener(this);

        eventListComposite = new EventListComposite(sailingService, eventSelectionProvider, errorReporter, stringMessages);
        eventListComposite.ensureDebugId("EventListComposite");
    }

    @Override
    protected List<UUID> getResult() {
        return selectedEvents;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        panel.add(eventListComposite);

        return panel;
    }

    @Override
    public void onEventSelectionChange(List<UUID> selectedEvents) {
        this.selectedEvents = selectedEvents;
    }
}
