package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaMediaTabView extends Composite implements
        TabView<RegattaMediaPlace, EventContext, EventRegattaView.Presenter> {

    public RegattaMediaTabView() {

    }

    @Override
    public Class<RegattaMediaPlace> getPlaceClassForActivation() {
        return RegattaMediaPlace.class;
    }

    @Override
    public void start(RegattaMediaPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaMediaTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattaMediaPlace placeToFire(EventContext ctx) {
        return new RegattaMediaPlace(ctx);
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter presenter) {
        // TODO Auto-generated method stub

    }

}