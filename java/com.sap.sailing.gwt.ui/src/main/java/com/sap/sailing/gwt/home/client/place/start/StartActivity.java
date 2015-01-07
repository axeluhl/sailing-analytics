package com.sap.sailing.gwt.home.client.place.start;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.home.client.shared.stage.StageEventType;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;

public class StartActivity extends AbstractActivity {
    private final StartClientFactory clientFactory;
    private final StartPlace place;

    public StartActivity(StartPlace place, StartClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new Placeholder());
        clientFactory.getSailingService().getPublicEventsOfAllSailingServers(new AsyncCallback<List<EventBaseDTO>>() {
            @Override
            public void onSuccess(List<EventBaseDTO> result) {
                final StartView view = clientFactory.createStartView();
                panel.setWidget(view.asWidget());
                Window.setTitle(place.getTitle());
                fillStartPageEvents(view, result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    protected void fillStartPageEvents(final StartView view, List<EventBaseDTO> events) {
        List<Pair<StageEventType, EventBaseDTO>> featuredEvents = new ArrayList<Pair<StageEventType, EventBaseDTO>>();
        List<EventBaseDTO> recentEventsOfLast12Month = new ArrayList<EventBaseDTO>();
        TimePoint now = MillisecondsTimePoint.now();
        final int MAX_STAGE_EVENTS = 5;
        for (EventBaseDTO event : events) {
            if (event.startDate != null && event.endDate != null) {
                if (now.after(new MillisecondsTimePoint(event.startDate)) && now.before(new MillisecondsTimePoint(event.endDate))) {
                    featuredEvents.add(new Pair<StageEventType, EventBaseDTO>(StageEventType.RUNNING, event));
                } else if (new MillisecondsTimePoint(event.startDate).after(now) &&
                        new MillisecondsTimePoint(event.startDate).before(now.plus(Duration.ONE_WEEK.times(4)))) {
                    featuredEvents.add(new Pair<StageEventType, EventBaseDTO>(StageEventType.UPCOMING_SOON, event));
                } else if (new MillisecondsTimePoint(event.endDate).before(now) &&
                        new MillisecondsTimePoint(event.endDate).after(now.minus(Duration.ONE_YEAR))) {
                    recentEventsOfLast12Month.add(event);
                    featuredEvents.add(new Pair<StageEventType, EventBaseDTO>(StageEventType.POPULAR, event));
                }
            }
        }
        Collections.sort(featuredEvents, new FeaturedEventsComparator());
        featuredEvents = featuredEvents.subList(0, MAX_STAGE_EVENTS);
        view.setFeaturedEvents(featuredEvents);
        Collections.sort(recentEventsOfLast12Month, new Comparator<EventBaseDTO>() {
            @Override
            public int compare(EventBaseDTO o1, EventBaseDTO o2) {
                final long diff = o2.endDate.getTime() - o1.endDate.getTime();
                return diff > 0l ? 1 : diff < 0l ? -1 : 0;
            }
        });
        view.setRecentEvents(recentEventsOfLast12Month);
    }
    
    /**
     * Sorts events by their time-wise distance from the current point in time.
     * 
     * @author Frank
     * @author Axel Uhl
     */
    private class FeaturedEventsComparator implements Comparator<Pair<StageEventType, EventBaseDTO>> {
        @Override
        public int compare(Pair<StageEventType, EventBaseDTO> eventAndStageType1,
                Pair<StageEventType, EventBaseDTO> eventAndStageType2) {
            TimePoint now = MillisecondsTimePoint.now();
            TimeRange event1Range = new TimeRangeImpl(new MillisecondsTimePoint(eventAndStageType1.getB().startDate), new MillisecondsTimePoint(eventAndStageType1.getB().endDate));
            TimeRange event2Range = new TimeRangeImpl(new MillisecondsTimePoint(eventAndStageType2.getB().startDate), new MillisecondsTimePoint(eventAndStageType2.getB().endDate));
            return event1Range.timeDifference(now).compareTo(event2Range.timeDifference(now));
        }
    }

}
