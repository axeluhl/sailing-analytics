package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MediaWidgetAndActivity extends Composite implements
        TabActivity<MediaPlace, EventContext, EventRegattaView.Presenter> {

    public MediaWidgetAndActivity() {

    }

    @Override
    public Class<MediaPlace> getPlaceClassForActivation() {
        return MediaPlace.class;
    }

    @Override
    public void start(MediaPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MediaWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public MediaPlace placeToFire(EventContext ctx) {
        return new MediaPlace(ctx);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        // TODO Auto-generated method stub

    }

}