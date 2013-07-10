package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.util.Collection;

import slash.navigation.gpx.GpxRoute;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.trackfiles.common.FormatNotSupportedException;

public class WindDataRetriever extends AbstractDataRetriever {

	@Override
	public Collection<GpxRoute> getRoutes(TrackFilesFormat format,
			final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes) throws FormatNotSupportedException, IOException {

		TrackReaderRetriever<WindSource, Wind> retriever = new TrackReaderRetriever<WindSource, Wind>() {
			@Override
			public TrackReader<WindSource, Wind> retrieveTrackReader(
					WindSource e) {
				return new GPSFixTrackReader<WindSource, Wind>(
						race.getOrCreateWindTrack(e));
			}
		};

		return getRoutes(race, dataBeforeAfter, rawFixes,
				WindToGpxPosition.INSTANCE,
				race.getWindSources(WindSourceType.TRACK_BASED_ESTIMATION),
				new NameReader<WindSource>() {
					@Override
					public String getName(WindSource s) {
						return s.name();
					}
				}, retriever);
	}

}
