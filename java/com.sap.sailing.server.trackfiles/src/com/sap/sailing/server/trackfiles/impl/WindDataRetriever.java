package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.TrackedRace;

public class WindDataRetriever extends AbstractDataRetriever {

    @Override
    public Collection<GpxRoute> getRoutes(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<WindSource, Wind> retriever = new TrackReaderRetriever<WindSource, Wind>() {
            @Override
            public TrackReader<WindSource, Wind> retrieveTrackReader(WindSource e) {
                return new TrackReaderImpl<WindSource, Wind>(race.getOrCreateWindTrack(e));
            }
        };

        return getRoutes(race, dataBeforeAfter, rawFixes, WindToGpxPosition.INSTANCE,
                Collections.<WindSource> singleton(new WindSourceImpl(WindSourceType.COMBINED)),
                new NameReader<WindSource>() {
                    @Override
                    public String getName(WindSource s) {
                        return s.name();
                    }
                }, retriever);
    }

}
