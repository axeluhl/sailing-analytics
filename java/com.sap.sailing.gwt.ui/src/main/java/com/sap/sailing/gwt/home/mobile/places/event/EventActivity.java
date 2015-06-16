package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.EventView.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;

public class EventActivity extends AbstractActivity implements Presenter {
    private final MobileApplicationClientFactory clientFactory;
    private final AbstractEventPlace place;
    private UUID currentEventUUId;
    
    public EventActivity(AbstractEventPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        if(place.getCtx().getEventDTO() == null) {
            currentEventUUId = UUID.fromString(place.getCtx().getEventId());
            
            clientFactory.getDispatch().execute(new GetEventViewAction(currentEventUUId), new AsyncCallback<EventViewDTO>() {
                @Override
                public void onSuccess(final EventViewDTO event) {
                    place.getCtx().updateContext(event);
                    initUi(panel, eventBus);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    // TODO @FM: extract text?
                    ErrorPlace errorPlace = new ErrorPlace("Error while loading the event with service getEventViewById()");
                    // TODO @FM: reload sinnvoll hier?
                    errorPlace.setComingFrom(place);
                    clientFactory.getPlaceController().goTo(errorPlace);
                }
            });
        } else {
            initUi(panel, eventBus);
        }
    }
    
    private void initUi(final AcceptsOneWidget panel, EventBus eventBus) {
        final EventView view = new EventViewImpl(this);
        panel.setWidget(view.asWidget());
        String sailorInfoUrl = place.getCtx().getEventDTO().getSailorsInfoURL();
        if (sailorInfoUrl != null && !sailorInfoUrl.isEmpty()) {
            view.setSailorInfos(StringMessages.INSTANCE.sailorInfoLongText(), StringMessages.INSTANCE.sailorInfo(), sailorInfoUrl);
        }
        view.setQuickFinderValues(place.getCtx().getEventDTO().getRegattas());
        view.getQuickfinder().addSelectionHandler(new QuickfinderSelectionHandler());
        view.setNavigator(clientFactory.getNavigator());
        clientFactory.getHomeService().getMediaForEvent(currentEventUUId, new AsyncCallback<MediaDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Failed to load media");
            }

            @Override
            public void onSuccess(MediaDTO result) {
                view.setMediaForImpressions(result.getPhotos().size(), result.getVideos().size(), result.getPhotos());
            }
        });
    }
    
    @Override
    public EventContext getCtx() {
        return place.getCtx();
    }

    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    
    private class QuickfinderSelectionHandler implements SelectionHandler<String> {
        @Override
        public void onSelection(SelectionEvent<String> event) {
            // TODO Link to correct places
            clientFactory.getPlaceController().goTo(new StartPlace());
        }
    }

    @Override
    public PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRaceViewerURL(String regattaName, String trackedRaceName) {
        // TODO Auto-generated method stub
        return null;
    }
}
