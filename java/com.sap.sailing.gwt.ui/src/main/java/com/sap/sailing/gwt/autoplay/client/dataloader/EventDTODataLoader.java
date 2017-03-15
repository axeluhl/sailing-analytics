package com.sap.sailing.gwt.autoplay.client.dataloader;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventDTODataLoader extends AutoPlayDataLoaderBase<AutoPlayClientFactorySixtyInch> {

    public EventDTODataLoader() {
        setLoadingIntervallInMs(10000);
    }

    @Override
    protected void doLoadData() {
        GWT.log("Loading eventDTO new");
        UUID eventUUID = getClientFactory().getSlideCtx().getSettings().getEventId();
        getClientFactory().getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                GWT.log("Event loaded " + event);
                getClientFactory().getSlideCtx().updateEvent(event);
            }

            @Override
            public void onFailure(Throwable caught) {
                getEventBus().fireEvent(new DataLoadFailureEvent(EventDTODataLoader.this, caught,
                        "Error loading Event with id " + eventUUID));
            }
        });
    }

    @Override
    protected void onStoppedLoading() {
    }

    @Override
    protected void onStartedLoading() {
    }

}
