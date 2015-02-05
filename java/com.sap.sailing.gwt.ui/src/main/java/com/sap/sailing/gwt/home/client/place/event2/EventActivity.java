package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.app.ApplicationClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class EventActivity extends AbstractActivity implements EventView.Presenter {

    private final EventPlace currentPlace;

    private final EventContext ctx = new EventContext();

    private EventView currentView = new TabletAndDesktopEventView();

    private ApplicationClientFactory clientFactory;

    public EventActivity(EventPlace place, ApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;

    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        // simple workflow: set widget, register ourself with the view, trigger tab navigation
        currentView.registerPresenter(this);
        panel.setWidget(currentView);

        if (currentPlace.getCtx().getEventDTO() != null) {
            ctx.updateContext(currentPlace.getCtx().getEventDTO());
            currentView.navigateTabsTo(currentPlace);
        } else {

            // async call to load eventDto
            // TODO: load data

        }

    }

    @Override
    public EventContext getCtx() {

        return ctx;
    }

}
