package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaMediaPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.EventView.Presenter;
import com.sap.sailing.gwt.home.mobile.places.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetEventViewAction;
import com.sap.sailing.gwt.ui.shared.dispatch.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
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
        if (getCtx().getEventDTO().isHasMedia()) {
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
    }
    
    @Override
    public EventContext getCtx() {
        return place.getCtx();
    }

    public DispatchSystem getDispatch() {
        return clientFactory.getDispatch();
    }
    


    @Override
    public PlaceNavigation<?> getMediaPageNavigation() {
        if (getCtx().getEventDTO().getType() == EventType.MULTI_REGATTA) {
            return clientFactory.getNavigator().getEventNavigation(new MultiregattaMediaPlace(getCtx()), null, false);
        } else {
            return clientFactory.getNavigator().getEventNavigation(new RegattaMediaPlace(getCtx()), null, false);
        }
    }

    @Override
    public PlaceNavigation<?> getNewsEntryPlaceNavigation(NewsEntryDTO entry) {
        if(entry instanceof LeaderboardNewsEntryDTO) {
            final LeaderboardNewsEntryDTO dto = (LeaderboardNewsEntryDTO) entry;
            return getRegattaLeaderboardNavigation(dto.getLeaderboardName());
        } 
        return null;
    }

    @Override
    public String getRaceViewerURL(String regattaName, String trackedRaceName) {
        return null;
    }

    @Override
    public PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName) {
        EventContext ctx = new EventContext(getCtx()).withRegattaId(leaderboardName).withRegattaAnalyticsManager(null);
        return clientFactory.getNavigator().getEventNavigation(new RegattaLeaderboardPlace(ctx), null, false);
    }

    @Override
    public void gotoNewsPlace(List<NewsEntryDTO> values) {
        clientFactory.getPlaceController().goTo(new LatestNewsPlace(place.getCtx(), values));
    }


}
