package com.sap.sailing.gwt.home.client.place.event2;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.app.ApplicationClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventActivity extends AbstractActivity implements EventView.Presenter {

    private final EventPlace currentPlace;

    private final EventContext ctx;

    private EventView currentView = new TabletAndDesktopEventView();

    private ApplicationClientFactory clientFactory;

    public EventActivity(EventPlace place, ApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.ctx = place.getCtx();
        this.clientFactory = clientFactory;

    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        if (currentPlace.getCtx().getEventDTO() != null) {
            afterLoad(panel);
        } else {
            final UUID eventUUID = UUID.fromString(currentPlace.getCtx().getEventId());
            clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
                @Override
                public void onSuccess(final EventDTO event) {
                    if(event != null) {
                        ctx.updateContext(event);
                        afterLoad(panel);
                    } else {
                        // TODO
//                        createErrorView("No such event with UUID " + eventUUID, null, panel);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO
//                    createErrorView("Error while loading the event with service getEventById()", caught, panel);
                }
            }); 

        }

    }

    private void afterLoad(AcceptsOneWidget panel) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }

    @Override
    public EventContext getCtx() {

        return ctx;
    }

    @Override
    public void navigateTo(Place place) {
        // TODO Auto-generated method stub
        
    }

}
