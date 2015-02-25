package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.media;

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
public class MultiRegattaMediaWidgetAndActivity extends Composite implements
        TabActivity<MultiRegattaMediaPlace, EventContext, EventMultiregattaView.Presenter> {

    public MultiRegattaMediaWidgetAndActivity() {

    }

    @Override
    public Class<MultiRegattaMediaPlace> getPlaceClassForActivation() {
        return MultiRegattaMediaPlace.class;
    }

    @Override
    public void start(MultiRegattaMediaPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiRegattaMediaWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MultiRegattaMediaPlace placeToFire(EventContext ctx) {
        return new MultiRegattaMediaPlace(ctx);
    }

    @Override
    public void setPresenter(EventMultiregattaView.Presenter presenter) {
        // TODO Auto-generated method stub

    }

}