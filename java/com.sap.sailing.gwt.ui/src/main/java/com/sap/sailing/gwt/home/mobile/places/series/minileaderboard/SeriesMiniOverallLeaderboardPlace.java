package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public class SeriesMiniOverallLeaderboardPlace extends AbstractSeriesPlace implements HasLocationTitle, HasMobileVersion {
    public SeriesMiniOverallLeaderboardPlace(String id) {
        super(id);
    }
    
    public SeriesMiniOverallLeaderboardPlace(SeriesContext context) {
        super(context);
    }

    @Override
    public String getLocationTitle() {
        return TextMessages.INSTANCE.series();
    }

    @Prefix(EventPrefixes.EventSeriesMiniOverallLeaderboard)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesMiniOverallLeaderboardPlace> {
        @Override
        protected SeriesMiniOverallLeaderboardPlace getRealPlace(SeriesContext context) {
            return new SeriesMiniOverallLeaderboardPlace(context);
        }
    }
}
