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
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.start.EventQuickfinderDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.start.GetRecentEventsAction;
import com.sap.sailing.gwt.ui.shared.dispatch.start.GetStagedEventsAction;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

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
        final StartView view = new StartViewImpl(StartActivity.this);
        clientFactory.getDispatch().execute(new GetStagedEventsAction(), new AsyncCallback<ListResult<EventStageDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void onSuccess(ListResult<EventStageDTO> result) {
                panel.setWidget(view.asWidget());
                Window.setTitle(place.getTitle());
                view.setFeaturedEvents(result.getValues());
            }
        });
        clientFactory.getDispatch().execute(new GetRecentEventsAction(15), new AsyncCallback<ListResult<EventQuickfinderDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }
            
            @Override
            public void onSuccess(ListResult<EventQuickfinderDTO> result) {
                view.setQuickFinderValues(result.getValues());
            }
        });
    }

    @Override
    public MobilePlacesNavigator getNavigator() {
        return clientFactory.getNavigator();
    }

    @Override
    public PlaceNavigation<?> getEventNavigation(EventQuickfinderDTO event) {
        return clientFactory.getNavigator().getEventNavigation(event.getId().toString(), event.getBaseURL(), event.isOnRemoteServer());
    }
}
