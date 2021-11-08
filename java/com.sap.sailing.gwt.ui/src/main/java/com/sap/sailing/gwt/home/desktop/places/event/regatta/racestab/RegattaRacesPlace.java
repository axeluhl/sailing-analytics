package com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab;

import java.util.Optional;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class RegattaRacesPlace extends AbstractEventRegattaPlace implements HasMobileVersion {

    private final String preferredSeriesName;

    public RegattaRacesPlace(String id, String leaderboardName) {
        super(id, leaderboardName);
        this.preferredSeriesName = null;
    }
    
    public RegattaRacesPlace(EventContext context) {
        this(context, null);
    }

    public RegattaRacesPlace(EventContext context, String preferredSeriesName) {
        super(context);
        this.preferredSeriesName = preferredSeriesName;
    }

    public final Optional<String> getPreferredSeriesName() {
        return Optional.ofNullable(preferredSeriesName);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaRacesPlace(ctx, null);
    }

    @Prefix(PlaceTokenPrefixes.RegattaRaces)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaRacesPlace> {
        @Override
        protected RegattaRacesPlace getRealPlace(EventContext context) {
            return new RegattaRacesPlace(context, null);
        }
    }
}
