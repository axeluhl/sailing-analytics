package com.sap.sailing.gwt.home.mobile.places.latestnews;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class LatestNewsPlace extends Place implements HasLocationTitle, HasMobileVersion {
    private List<NewsEntryDTO> news = new LinkedList<NewsEntryDTO>();
    private final EventContext ctx;

    public LatestNewsPlace(String eventUuidAsString) {
        this.ctx = new EventContext();
        ctx.withId(eventUuidAsString);
    }

    public LatestNewsPlace(EventContext ctx, Collection<NewsEntryDTO> newsEntries) {
        news.addAll(newsEntries);
        this.ctx = ctx;
    }

    public List<NewsEntryDTO> getNews() {
        return news;
    }

    public String getTitle() {
        return TextMessages.INSTANCE.sapSailing();
    }
    
    @Prefix(EventPrefixes.EventNews)
    public static class Tokenizer implements PlaceTokenizer<LatestNewsPlace> {
        @Override
        public String getToken(LatestNewsPlace place) {
            return place.getCtx().getEventId();
        }

        @Override
        public LatestNewsPlace getPlace(String token) {
            if (token == null || token.isEmpty()) {
                return null;
            }
            return new LatestNewsPlace(token);
        }
    }

    @Override
    public String getLocationTitle() {
        return TextMessages.INSTANCE.headerLogo();
    }

    public EventContext getCtx() {
        return ctx;
    }
}
