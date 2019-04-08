package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SeriesMiniOverallLeaderboardPlace extends AbstractSeriesPlace implements HasLocationTitle, HasMobileVersion {
    public SeriesMiniOverallLeaderboardPlace(SeriesContext context) {
        super(context);
    }

    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.series();
    }

    @Prefix(PlaceTokenPrefixes.EventSeriesMiniOverallLeaderboard)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesMiniOverallLeaderboardPlace> {
        @Override
        protected SeriesMiniOverallLeaderboardPlace getRealPlace(SeriesContext context) {
            return new SeriesMiniOverallLeaderboardPlace(context);
        }
    }
}
