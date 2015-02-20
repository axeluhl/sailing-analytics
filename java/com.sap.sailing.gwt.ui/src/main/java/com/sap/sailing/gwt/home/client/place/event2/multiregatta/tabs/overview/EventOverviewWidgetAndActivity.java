package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventOverviewWidgetAndActivity extends Composite implements TabActivity<EventOverviewPlace, EventContext, EventMultiregattaView.Presenter> {

    public EventOverviewWidgetAndActivity() {

    }

    @Override
    public Class<EventOverviewPlace> getPlaceClassForActivation() {
        return EventOverviewPlace.class;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(EventOverviewPlace myPlace, AcceptsOneWidget contentArea) {

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
    public EventOverviewPlace placeToFire(EventContext ctx) {
        return new EventOverviewPlace(ctx);
    }

}