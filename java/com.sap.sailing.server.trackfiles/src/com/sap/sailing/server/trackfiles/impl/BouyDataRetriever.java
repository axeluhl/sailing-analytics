package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.Collection;

import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.TrackedRace;

public class BouyDataRetriever extends AbstractDataRetriever {

    @Override
    public Collection<GpxRoute> getRoutes(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Mark, GPSFix> retriever = new TrackReaderRetriever<Mark, GPSFix>() {
            @Override
            public TrackReader<Mark, GPSFix> retrieveTrackReader(Mark e) {
                return new TrackReaderImpl<Mark, GPSFix>(race.getOrCreateTrack(e));
            }
        };

        return getRoutes(race, dataBeforeAfter, rawFixes, GPSFixToGpxPosition.INSTANCE, race.getMarks(),
                new NameReader<Mark>() {
                    @Override
                    public String getName(Mark m) {
                        return m.getName();
                    }
                }, retriever);
    }

}
