package com.sap.sailing.gwt.home.client.place.event2.tabs.races;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventRacesWidgetAndActivity extends Composite implements TabActivity<EventRacesPlace, EventContext, EventRegattaView.Presenter> {

    public EventRacesWidgetAndActivity() {

    }

    @Override
    public Class<EventRacesPlace> getPlaceClassForActivation() {
        return EventRacesPlace.class;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(EventRacesPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, EventRacesWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public EventRacesPlace placeToFire(EventContext ctx) {
        return new EventRacesPlace(ctx);
    }

}