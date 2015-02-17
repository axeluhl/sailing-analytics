package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public abstract class EventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    public EventActivity(PLACE place, EventClientFactory clientFactory) {
        this.currentPlace = place;
        this.ctx = new EventContext(clientFactory, place.getCtx());

        this.clientFactory = clientFactory;
    }

    @Override
    public EventContext getCtx() {

        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabActivity<?, EventContext> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire(ctx);
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

}
