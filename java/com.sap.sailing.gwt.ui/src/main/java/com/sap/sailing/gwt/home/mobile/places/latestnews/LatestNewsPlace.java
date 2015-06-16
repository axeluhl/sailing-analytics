package com.sap.sailing.gwt.home.mobile.places.latestnews;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.shared.dispatch.news.NewsEntryDTO;

public class LatestNewsPlace extends Place implements HasLocationTitle, HasMobileVersion {
    private List<NewsEntryDTO> news = new LinkedList<NewsEntryDTO>();

    public LatestNewsPlace() {
    }

    public LatestNewsPlace(Collection<NewsEntryDTO> newsEntries) {
        news.addAll(newsEntries);
    }

    public List<NewsEntryDTO> getNews() {
        return news;
    }

    public String getTitle() {
        return TextMessages.INSTANCE.sapSailing();
    }
    
    public static class Tokenizer implements PlaceTokenizer<LatestNewsPlace> {
        @Override
        public String getToken(LatestNewsPlace place) {
            return null;
        }

        @Override
        public LatestNewsPlace getPlace(String token) {
            return new LatestNewsPlace();
        }
    }

    @Override
    public String getLocationTitle() {
        return TextMessages.INSTANCE.headerLogo();
    }
}
