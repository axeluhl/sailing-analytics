package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.TrackedRace;

import slash.navigation.gpx.GpxRoute;

public class CompetitorDataRetriever extends AbstractDataRetriever {

    @Override
    public Collection<GpxRoute> getRoutes(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Map.Entry<Competitor, Boat>, GPSFixMoving> retriever = new TrackReaderRetriever<Map.Entry<Competitor, Boat>, GPSFixMoving>() {
            @Override
            public TrackReader<Map.Entry<Competitor, Boat>, GPSFixMoving> retrieveTrackReader(Map.Entry<Competitor, Boat> e) {
                return new TrackReaderImpl<Map.Entry<Competitor, Boat>, GPSFixMoving>(race.getTrack(e.getKey()));
            }
        };
 
        return getRoutes(race, dataBeforeAfter, rawFixes, GPSFixMovingToGpxPosition.INSTANCE, race.getRace()
                .getCompetitorsAndTheirBoats().entrySet(), new NameReader<Map.Entry<Competitor, Boat>>() {
            @Override
            public String getName(Map.Entry<Competitor, Boat> competitorAndBoat) {
                final Competitor c = competitorAndBoat.getKey();
                final Boat b = competitorAndBoat.getValue();
                return c.getName() + " - " + b.getSailID() + " - "
                        + c.getTeam().getNationality().getThreeLetterIOCAcronym();
            }
        }, retriever);
    }

}
