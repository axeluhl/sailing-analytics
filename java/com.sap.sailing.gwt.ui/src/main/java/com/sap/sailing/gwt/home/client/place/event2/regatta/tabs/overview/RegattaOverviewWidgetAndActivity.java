package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaOverviewWidgetAndActivity extends Composite implements
        TabActivity<RegattaOverviewPlace, EventContext, EventRegattaView.Presenter> {

    public RegattaOverviewWidgetAndActivity() {

    }

    @Override
    public Class<RegattaOverviewPlace> getPlaceClassForActivation() {
        return RegattaOverviewPlace.class;
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaOverviewWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattaOverviewPlace placeToFire(EventContext ctx) {
        return new RegattaOverviewPlace(ctx);
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter presenter) {
        // TODO Auto-generated method stub

    }

}