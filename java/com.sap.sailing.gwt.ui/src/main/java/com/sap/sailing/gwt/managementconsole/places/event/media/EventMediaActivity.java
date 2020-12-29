package com.sap.sailing.gwt.managementconsole.places.event.media;

import java.util.UUID;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventMediaActivity extends AbstractManagementConsoleActivity<EventMediaPlace>
        implements EventMediaView.Presenter {


    public EventMediaActivity(final ManagementConsoleClientFactory clientFactory, final EventMediaPlace place) {
        super(clientFactory, place);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final EventMediaView view = getClientFactory().getViewFactory().getEventMediaView();
        view.setPresenter(this);
        container.setWidget(view);

        final UUID eventId = getPlace().getEventId();
        getClientFactory().getSailingService().getEventById(eventId, false, new AsyncCallback<EventDTO>() {

            @Override
            public void onSuccess(final EventDTO result) {
                view.setEvent(result);
            }

            @Override
            public void onFailure(final Throwable caught) {
                getClientFactory().getErrorReporter().reportError("Failed to load event " + eventId);
            }
        });
    }

}
