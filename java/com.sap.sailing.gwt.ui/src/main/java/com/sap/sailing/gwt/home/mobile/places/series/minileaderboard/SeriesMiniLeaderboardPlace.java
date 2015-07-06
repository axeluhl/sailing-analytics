package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventPrefixes;
import com.sap.sailing.gwt.home.client.place.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class SeriesMiniLeaderboardPlace extends AbstractSeriesPlace implements HasLocationTitle, HasMobileVersion {
    public SeriesMiniLeaderboardPlace(String id) {
        super(id);
    }
    
    public SeriesMiniLeaderboardPlace(SeriesContext context) {
        super(context);
    }

    @Override
    public String getLocationTitle() {
        return TextMessages.INSTANCE.series();
    }

    @Prefix(EventPrefixes.RegattaMiniLeaderboard)
    public static class Tokenizer extends AbstractSeriesPlace.Tokenizer<SeriesMiniLeaderboardPlace> {
        @Override
        protected SeriesMiniLeaderboardPlace getRealPlace(SeriesContext context) {
            return new SeriesMiniLeaderboardPlace(context);
        }
    }
}
