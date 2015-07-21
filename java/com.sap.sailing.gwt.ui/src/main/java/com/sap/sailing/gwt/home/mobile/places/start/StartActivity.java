package com.sap.sailing.gwt.home.mobile.places.start;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.mobile.places.start.StartView.Presenter;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;

public class StartActivity extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final StartPlace place;

    public StartActivity(StartPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        Window.setTitle(place.getTitle());
        clientFactory.getHomeService().getStartView(new AsyncCallback<StartViewDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(StartViewDTO result) {
                final StartView view = new StartViewImpl(StartActivity.this);
                panel.setWidget(view.asWidget());
                Window.setTitle(place.getTitle());
                view.setData(result);
            }
        });
    }

    @Override
    public MobilePlacesNavigator getNavigator() {
        return clientFactory.getNavigator();
    }

}
