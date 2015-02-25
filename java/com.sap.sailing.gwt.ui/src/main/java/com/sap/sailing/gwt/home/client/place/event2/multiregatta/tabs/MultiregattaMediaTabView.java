package com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.MultiregattaTabView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class MultiregattaMediaTabView extends Composite implements MultiregattaTabView<MultiregattaMediaPlace> {

    public MultiregattaMediaTabView() {

    }

    @Override
    public Class<MultiregattaMediaPlace> getPlaceClassForActivation() {
        return MultiregattaMediaPlace.class;
    }

    @Override
    public void start(MultiregattaMediaPlace myPlace, AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));

        contentArea.setWidget(this);
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, MultiregattaMediaTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    @Override
    public MultiregattaMediaPlace placeToFire() {
        return new MultiregattaMediaPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventMultiregattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

}