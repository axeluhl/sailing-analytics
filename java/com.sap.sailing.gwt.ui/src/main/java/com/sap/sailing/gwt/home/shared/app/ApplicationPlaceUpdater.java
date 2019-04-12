package com.sap.sailing.gwt.home.shared.app;

import java.util.UUID;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.RegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.mediatab.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.regattastab.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.analyticstab.RegattaCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.mediatab.RegattaMediaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.analyticstab.EventSeriesCompetitorAnalyticsPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.leaderboardstab.EventSeriesLeaderboardsPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.overallleaderboardtab.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;

/**
 * Helper class to support converting old places to new ones and relace inconsistent places with working ones.
 */
public class ApplicationPlaceUpdater {

    public Place getRealPlace(Place place) {
        if (place instanceof EventPlace) {
            return getRealEventPlace((EventPlace) place);
        } else if (place instanceof RegattaPlace) {
            return getRealRegattaPlace((RegattaPlace) place);
        } else if (place instanceof SeriesPlace) {
            return getRealSeriesPlace((SeriesPlace) place);
        }
        return place;
    }

    private Place getRealRegattaPlace(RegattaPlace place) {
        String eventId = place.getEventUuidAsString();
        if (eventId == null || eventId.isEmpty()) {
            return new EventsPlace();
        }
        EventContext eventContext = new EventContext().withId(eventId);
        String regattaId = place.getLeaderboardIdAsNameString();
        boolean hasRegattaId = (regattaId != null && !regattaId.isEmpty());

        // TODO evaluate additional parameters

        if (hasRegattaId) {
            switch (place.getNavigationTab()) {
            case CompetitorAnalytics:
                return new RegattaCompetitorAnalyticsPlace(eventContext);
            case Leaderboard:
                return new RegattaLeaderboardPlace(eventContext);
            }
        }
        return new EventDefaultPlace(eventContext);
    }

    @SuppressWarnings("incomplete-switch")
    private Place getRealEventPlace(EventPlace place) {
        String eventId = place.getEventUuidAsString();
        if (eventId == null || eventId.isEmpty()) {
            return new EventsPlace();
        }
        EventContext eventContext = new EventContext().withId(eventId);
        String regattaId = place.getLeaderboardIdAsNameString();
        boolean hasRegattaId = (regattaId != null && !regattaId.isEmpty());
        if (hasRegattaId) {
            eventContext.withRegattaId(regattaId);

            switch (place.getNavigationTab()) {
            case Media:
                return new RegattaMediaPlace(eventContext);
            case Overview:
                return new RegattaOverviewPlace(eventContext);
            case Regatta:
                return new RegattaRacesPlace(eventContext);
            }
        } else {
            switch (place.getNavigationTab()) {
            // TODO some places aren't implemented yet
            case Media:
                return new MultiregattaMediaPlace(eventContext);
            case Overview:
                return new EventDefaultPlace(eventContext);
            case Schedule:
                // Schedule not implemented yet -> using race list
            case Regattas:
                return new MultiregattaRegattasPlace(eventContext);
            }
        }

        return new EventDefaultPlace(eventContext);
    }

    private Place getRealSeriesPlace(SeriesPlace place) {
        String seriesId = place.getEventUuidAsString();
        SeriesContext context = SeriesContext.createWithSeriesId(UUID.fromString(seriesId));

        // TODO evaluate additional parameters

        switch (place.getNavigationTab()) {
        case OverallLeaderboard:
            return new EventSeriesOverallLeaderboardPlace(context);
        case RegattaLeaderboards:
            return new EventSeriesLeaderboardsPlace(context);
        case CompetitorAnalytics:
            return new EventSeriesCompetitorAnalyticsPlace(context);
        }
        return new SeriesDefaultPlace(context);
    }
}
