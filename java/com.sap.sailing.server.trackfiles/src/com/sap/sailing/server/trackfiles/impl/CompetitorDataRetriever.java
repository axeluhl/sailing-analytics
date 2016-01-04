package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.Collection;

import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CompetitorDataRetriever extends AbstractDataRetriever {

    @Override
    public Collection<GpxRoute> getRoutes(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Competitor, GPSFixMoving> retriever = new TrackReaderRetriever<Competitor, GPSFixMoving>() {
            @Override
            public TrackReader<Competitor, GPSFixMoving> retrieveTrackReader(Competitor e) {
                return new TrackReaderImpl<Competitor, GPSFixMoving>(race.getTrack(e));
            }
        };

        return getRoutes(race, dataBeforeAfter, rawFixes, GPSFixMovingToGpxPosition.INSTANCE, race.getRace()
                .getCompetitors(), new NameReader<Competitor>() {
            @Override
            public String getName(Competitor c) {
                return c.getName() + " - " + c.getBoat().getSailID() + " - "
                        + c.getTeam().getNationality().getThreeLetterIOCAcronym();
            }
        }, retriever);
    }

}
