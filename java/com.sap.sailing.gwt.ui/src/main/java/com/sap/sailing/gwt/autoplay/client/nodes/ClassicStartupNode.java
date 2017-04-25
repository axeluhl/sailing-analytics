package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.nodes.base.BaseCompositeNode;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class ClassicStartupNode extends BaseCompositeNode {
    private AutoPlayClientFactory cf;
    private AutoPlayNode whenReadyNode;

    public ClassicStartupNode(final AutoPlayClientFactory cf) {
        this.cf = cf;
        whenReadyNode = new ClassicRootNode(cf, new LiveRaceLeaderboard(cf), new LiveRaceBoardNode(cf));
    }


    @Override
    public void onStart() {
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
}
