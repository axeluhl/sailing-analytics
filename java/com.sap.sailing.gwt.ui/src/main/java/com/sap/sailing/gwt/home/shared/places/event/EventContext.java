package com.sap.sailing.gwt.home.shared.places.event;

import com.sap.sailing.gwt.home.desktop.places.event.regatta.RegattaAnalyticsDataManager;

/**
 * Common context used by the different tabs in the event place.
 * 
 * @author pgtaboada
 *
 */
public class EventContext {
    private String eventId;
    private String regattaId;
    private RegattaAnalyticsDataManager regattaAnalyticsManager;

    public EventContext() {
    }

    public EventContext(EventContext ctx) {
        withId(ctx.eventId);
        withRegattaId(ctx.regattaId);
        withRegattaAnalyticsManager(ctx.regattaAnalyticsManager);
    }

    public EventContext withId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public EventContext withRegattaId(String regattaId) {
        this.regattaId = regattaId;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRegattaId() {
        return regattaId;
    }

    public RegattaAnalyticsDataManager getRegattaAnalyticsManager() {
        return regattaAnalyticsManager;
    }

    public EventContext withRegattaAnalyticsManager(RegattaAnalyticsDataManager regattaAnalyticsManager) {
        this.regattaAnalyticsManager = regattaAnalyticsManager;
        return this;
    }
}
