package com.sap.sailing.gwt.home.client.place.start;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.stage.StageEventType;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
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
        
        clientFactory.getSailingService().getPublicEventsOfAllSailingServers(new AsyncCallback<List<EventBaseDTO>>() {
            @Override
            public void onSuccess(List<EventBaseDTO> result) {
                fillStartPageEvents(view, result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    @SuppressWarnings("deprecation")
    protected void fillStartPageEvents(StartView view, List<EventBaseDTO> events) {
        List<Pair<StageEventType, EventBaseDTO>> featuredEvents = new ArrayList<Pair<StageEventType, EventBaseDTO>>();
        
        List<EventBaseDTO> recentEventsOfSameYear = new ArrayList<EventBaseDTO>();
        List<EventBaseDTO> upcomingSoonEvents = new ArrayList<EventBaseDTO>();
        List<EventBaseDTO> popularEvents = new ArrayList<EventBaseDTO>();
        Date now = new Date();
        int currentYear = now.getYear();
        final int MAX_STAGE_EVENTS = 2;
        final long FOUR_WEEK_IN_MS = 4L * (1000 * 60 * 60 * 24 * 7);
        
        for(EventBaseDTO event: events) {
            if(event.startDate != null && event.endDate != null) {
                if (now.after(event.startDate) && now.before(event.endDate)) {
                    featuredEvents.add(new Pair<StageEventType, EventBaseDTO>(StageEventType.RUNNING, event));
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

        Collections.sort(featuredEvents, new FeaturedEventsComparator());

        view.setFeaturedEvents(featuredEvents);
        view.setRecentEvents(recentEventsOfSameYear);
    }
    
    private void fillingUpEventsList(int maxAmountOfElements, List<Pair<StageEventType, EventBaseDTO>> resultList, StageEventType type, List<EventBaseDTO> listToTakeElementsFrom) {
        int maxElementsToFill = maxAmountOfElements - resultList.size();
        int elementsToTransfer = listToTakeElementsFrom.size() > maxElementsToFill ? maxElementsToFill : listToTakeElementsFrom.size();
        for(int i = 0; i < elementsToTransfer; i++) {
            resultList.add(new Pair<StageEventType, EventBaseDTO>(type, listToTakeElementsFrom.get(i)));
        }
    }
    
    /**
     * Comparator for sorting a pair of event and stageType first by order number of stage type and then by event start date.
     * @author Frank
     *
     */
    private class FeaturedEventsComparator implements Comparator<Pair<StageEventType, EventBaseDTO>> {

        @Override
        public int compare(Pair<StageEventType, EventBaseDTO> eventAndStageType1, Pair<StageEventType, EventBaseDTO> eventAndStageType2) {
            int result;
            if(eventAndStageType1.getA().ordinal() == eventAndStageType2.getA().ordinal()) {
                result = eventAndStageType1.getB().startDate.compareTo(eventAndStageType2.getB().startDate); 
            } else {
                result = eventAndStageType1.getA().ordinal() - eventAndStageType2.getA().ordinal();  
            }
            
            return result;
        }
    }

}
