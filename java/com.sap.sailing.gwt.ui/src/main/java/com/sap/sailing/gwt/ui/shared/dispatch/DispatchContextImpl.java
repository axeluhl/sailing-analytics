package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Date;
import java.util.Locale;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.news.EventNewsService;
import com.sap.sailing.server.RacingEventService;

@GwtIncompatible
public class DispatchContextImpl implements DispatchContext {
    private final RacingEventService racingEventService;
    private final EventNewsService eventNewsService;
    private final Date currentClientTime;
//    private final Date currentServerTime = new Date();
    private String clientLocaleName;

    public  DispatchContextImpl(Date currentClientTime, RacingEventService racingEventService, EventNewsService eventNewsService, String clientLocaleName) {
        this.currentClientTime = currentClientTime;
        this.racingEventService = racingEventService;
        this.eventNewsService = eventNewsService;
        this.clientLocaleName = clientLocaleName;
    }
    
    @Override
    public RacingEventService getRacingEventService() {
        return racingEventService;
    }
    
    public EventNewsService getEventNewsService() {
        return eventNewsService;
    }
    
    @Override
    public Date getCurrentClientTime() {
        return currentClientTime;
    }
    
//    public Date getCurrentServerTime() {
//        return currentServerTime;
//    }
    
    @Override
    public String getClientLocaleName() {
        return clientLocaleName;
    }
    
    @Override
    public Locale getClientLocale() {
        return Locale.forLanguageTag(clientLocaleName);
    }
}
