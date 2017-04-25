package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayClientFactoryClassic;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.BaseCompositeNode;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial.SixtyInchInitialPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class ClassicStartupNode extends BaseCompositeNode {
    private AutoPlayClientFactoryClassic cf;
    private AutoPlayNode whenReadyNode;

    public ClassicStartupNode(final AutoPlayClientFactoryClassic cf) {
        this.cf = cf;
    }


    @Override
    public void onStart() {
        cf.getEventBus().fireEvent(new PlaceChangeEvent(new SixtyInchInitialPlace()));
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
                        new AutoPlayFailureEvent(caught, "Error loading Event with id " + eventUUID));
            }
        });
    }

    public void setWhenReadyDestination(AutoPlayNode whenReadyNode) {
        this.whenReadyNode = whenReadyNode;
    }
}
