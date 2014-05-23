package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.start.StartPlace;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;

public class TabletAndDesktopApplicationClientFactory extends ClientFactoryImpl implements ApplicationClientFactory {

    @Override
    public Widget getRoot() {
        return new TabletAndDesktopApplicationView();
    }

    @Override
    public AcceptsOneWidget getStage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Place getDefaultPlace() {
        return new StartPlace();
    }
}
