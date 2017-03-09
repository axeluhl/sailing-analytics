package com.sap.sailing.gwt.autoplay.client.dataloader;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailure;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventDTODataLoader extends AutoPlayDataLoaderBase {
    protected EventDTO current;

    public EventDTODataLoader() {
        setLoadingIntervallInMs(10000);
    }

    @Override
    protected void onLoadData() {
        GWT.log("Loading eventDTO new");
        UUID eventUUID = getClientFactory().getSlideCtx().getSettings().getEventId();
        getClientFactory().getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                // TODO change detection
                getEventBus().fireEvent(new EventChanged(event));
                current = event;
            }

            @Override
            public void onFailure(Throwable caught) {
                getEventBus().fireEvent(new DataLoadFailure(EventDTODataLoader.this, caught,
                        "Error loading Event with id " + eventUUID));
            }
        }); 
    }

    @Override
    protected void onStoppedLoading() {
        current = null;
    }

    @Override
    protected void onStartedLoading() {
    }

}
