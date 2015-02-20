package com.sap.sailing.gwt.home.client.place.event2.tabs.overview;

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
public class EventOverviewWidgetAndActivity extends Composite implements TabActivity<EventRegattaOverviewPlace, EventContext, EventRegattaView.Presenter> {

    public EventOverviewWidgetAndActivity() {

    }

    @Override
    public Class<EventRegattaOverviewPlace> getPlaceClassForActivation() {
        return EventRegattaOverviewPlace.class;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(EventRegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, EventOverviewWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public EventRegattaOverviewPlace placeToFire(EventContext ctx) {
        return new EventRegattaOverviewPlace(ctx);
    }

}