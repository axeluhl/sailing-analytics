package com.sap.sailing.gwt.home.client.place.event2.tabs.schedule;

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
public class ScheduleWidgetAndActivity extends Composite implements TabActivity<SchedulePlace, EventContext> {

    public ScheduleWidgetAndActivity() {

    }

    @Override
    public Class<SchedulePlace> getPlaceClassForActivation() {
        return SchedulePlace.class;
    }

    @Override
    public void start(SchedulePlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, ScheduleWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public SchedulePlace placeToFire(EventContext ctx) {
        return new SchedulePlace(ctx);
    }

}