package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.news.InfoNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.LeaderboardNewsEntryDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;
import com.sap.sailing.news.EventNewsItem;
import com.sap.sailing.news.impl.InfoEventNewsItem;
import com.sap.sailing.news.impl.LeaderboardUpdateNewsItem;

public class GetEventOverviewNewsAction implements Action<ResultWithTTL<ListResult<NewsEntryDTO>>> {
    private UUID eventId;
    
    public GetEventOverviewNewsAction() {
    }

    public GetEventOverviewNewsAction(UUID eventId) {
        this.eventId = eventId;
    }
    
    @Override
    @GwtIncompatible
    public ResultWithTTL<ListResult<NewsEntryDTO>> execute(DispatchContext context) {
        Event event = context.getRacingEventService().getEvent(eventId);
        long ttl = 1000 * 60 * 2;
        return new ResultWithTTL<>(ttl, new ListResult<NewsEntryDTO>(getNews(context, event)));
    }
    
    @GwtIncompatible
    private List<NewsEntryDTO> getNews(DispatchContext dispatchContext, Event event) {
        List<EventNewsItem> newsItems = dispatchContext.getEventNewsService().getNews(event);

        List<NewsEntryDTO> news = new ArrayList<>(newsItems.size());
        for(EventNewsItem newsItem: newsItems) {
            if(newsItem instanceof InfoEventNewsItem) {
                news.add(new InfoNewsEntryDTO((InfoEventNewsItem) newsItem, dispatchContext.getClientLocale()));
            }
            if(newsItem instanceof LeaderboardUpdateNewsItem) {
                news.add(new LeaderboardNewsEntryDTO((LeaderboardUpdateNewsItem) newsItem));
            }
        }
        return news;
    }
}
