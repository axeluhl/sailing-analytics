package com.sap.sailing.gwt.home.client.place.event2.tabs.regattas;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattasWidgetAndActivity extends Composite implements TabActivity<RegattasPlace, EventContext> {

    public RegattasWidgetAndActivity() {

    }

    @Override
    public Class<RegattasPlace> getPlaceClassForActivation() {
        return RegattasPlace.class;
    }

    @Override
    public void start(RegattasPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattasWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattasPlace placeToFire(EventContext ctx) {
        return new RegattasPlace(ctx);
    }

}