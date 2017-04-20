package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl.BaseCompositeNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class StartupNode extends BaseCompositeNode {
    private AutoPlayClientFactorySixtyInch cf;
    private AutoPlayNode whenReadyNode;

    public StartupNode(final AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
    }


    @Override
    public void onStart() {
        cf.getEventBus().fireEvent(new PlaceChangeEvent(new SlideInitPlace()));
        final UUID eventUUID = cf.getSlideCtx().getSettings().getEventId();
        cf.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                GWT.log("Event loaded " + event);
                cf.getSlideCtx().updateEvent(event);
                transitionTo(whenReadyNode);
            }

            @Override
            public void onFailure(Throwable caught) {
                getBus().fireEvent(
                        new DataLoadFailureEvent(StartupNode.this, caught, "Error loading Event with id " + eventUUID));
            }
        });
    }

    public void setWhenReadyDestination(AutoPlayNode whenReadyNode) {
        this.whenReadyNode = whenReadyNode;
    }
}
