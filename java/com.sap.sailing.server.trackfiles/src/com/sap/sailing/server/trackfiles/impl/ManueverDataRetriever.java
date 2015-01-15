package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.Collection;

import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

public class ManueverDataRetriever extends AbstractDataRetriever {

    @Override
    public Collection<GpxRoute> getRoutes(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

        final TimePoint start = race.getStartOfRace() == null ? race.getStartOfTracking() : race.getStartOfRace();
        final TimePoint end = race.getEndOfRace() == null ? race.getEndOfTracking() : race.getEndOfRace();

        TrackReaderRetriever<Competitor, Maneuver> retriever = new TrackReaderRetriever<Competitor, Maneuver>() {
            @Override
            public TrackReader<Competitor, Maneuver> retrieveTrackReader(Competitor e) {
                return new TrackReader<Competitor, Maneuver>() {
                    @Override
                    public Iterable<Maneuver> getRawTrack(Competitor e) {
                        return race.getManeuvers(e, start, end, false);
                    }

                    @Override
                    public Iterable<Maneuver> getTrack(Competitor e) {
                        return race.getManeuvers(e, start, end, false);
                    }

                    @Override
                    public IterableLocker getLocker() {
                        return new NoIterableLocker();
                    }

                };
            }
        };

        return getRoutes(race, dataBeforeAfter, rawFixes, ManeuverToGpxPosition.INSTANCE, race.getRace()
                .getCompetitors(), new NameReader<Competitor>() {
            @Override
            public String getName(Competitor s) {
                return s.getName();
            }
        }, retriever);
    }

}
