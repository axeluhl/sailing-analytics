package com.sap.sailing.gwt.home.client.place.start;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.stage.StageEventType;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Util.Pair;

public class StartActivity extends AbstractActivity {
    private final StartClientFactory clientFactory;

    public StartActivity(StartPlace place, StartClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final StartView view = clientFactory.createStartView();
        panel.setWidget(view.asWidget());
        
        clientFactory.getSailingService().getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onSuccess(List<EventDTO> result) {
                fillStartPageEvents(view, result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    @SuppressWarnings("deprecation")
    protected void fillStartPageEvents(StartView view, List<EventDTO> events) {
        List<Pair<StageEventType, EventDTO>> featuredEvents = new ArrayList<Pair<StageEventType, EventDTO>>();
        
        List<EventDTO> recentEventsOfSameYear = new ArrayList<EventDTO>();
        List<EventDTO> upcomingSoonEvents = new ArrayList<EventDTO>();
        List<EventDTO> popularEvents = new ArrayList<EventDTO>();
        Date now = new Date();
        int currentYear = now.getYear();
        final int MAX_STAGE_EVENTS = 2;
        final long FOUR_WEEK_IN_MS = 4L * (1000 * 60 * 60 * 24 * 7);
        
        for(EventDTO event: events) {
            // ignore events with no start and end date
            if(event.startDate != null && event.endDate != null) {
                if (now.after(event.startDate) && now.before(event.endDate)) {
                    featuredEvents.add(new Pair<StageEventType, EventDTO>(StageEventType.RUNNING, event));
                } else if (event.startDate.after(now) && event.startDate.getTime() - now.getTime() < FOUR_WEEK_IN_MS) {
                    upcomingSoonEvents.add(event);
                } else if (event.endDate.before(now) && event.endDate.getYear() == currentYear) {
                    recentEventsOfSameYear.add(event);
                }
            }
        }

        if(featuredEvents.size() < MAX_STAGE_EVENTS) {
            fillingUpEventsList(MAX_STAGE_EVENTS, featuredEvents, StageEventType.UPCOMING_SOON, upcomingSoonEvents);
        }
        if(featuredEvents.size() < MAX_STAGE_EVENTS) {
            fillingUpEventsList(MAX_STAGE_EVENTS, featuredEvents, StageEventType.POPULAR, popularEvents);
        }
        // fallback for the case we did not find any events
        if(featuredEvents.size() < MAX_STAGE_EVENTS) {
            fillingUpEventsList(MAX_STAGE_EVENTS, featuredEvents, StageEventType.POPULAR, recentEventsOfSameYear);
        }

        view.setFeaturedEvents(featuredEvents);
        view.setRecentEvents(recentEventsOfSameYear);
    }
    
    private void fillingUpEventsList(int maxAmountOfElements, List<Pair<StageEventType, EventDTO>> resultList, StageEventType type, List<EventDTO> listToTakeElementsFrom) {
        int maxElementsToFill = maxAmountOfElements - resultList.size();
        int elementsToTransfer = listToTakeElementsFrom.size() > maxElementsToFill ? maxElementsToFill : listToTakeElementsFrom.size();
        for(int i = 0; i < elementsToTransfer; i++) {
            resultList.add(new Pair<StageEventType, EventDTO>(type, listToTakeElementsFrom.get(i)));
        }
    }
}
