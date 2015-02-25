package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiRegattaOverviewWidgetAndActivity extends Composite implements
        TabActivity<MultiRegattaOverviewPlace, EventContext, EventMultiregattaView.Presenter> {

    public MultiRegattaOverviewWidgetAndActivity() {

    }

    @Override
    public Class<MultiRegattaOverviewPlace> getPlaceClassForActivation() {
        return MultiRegattaOverviewPlace.class;
    }
    
    @Override
    public void setPresenter(EventMultiregattaView.Presenter presenter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(MultiRegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiRegattaOverviewWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MultiRegattaOverviewPlace placeToFire(EventContext ctx) {
        return new MultiRegattaOverviewPlace(ctx);
    }

}