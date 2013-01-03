package com.sap.sailing.domain.tracking.impl;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.Wind;

public class MarkPassingInferenceImpl implements RaceChangeListener{
	private final DynamicTrackedRace trackedRace;

	public MarkPassingInferenceImpl(DynamicTrackedRace trackedRace) {
		this.trackedRace = trackedRace;
		this.trackedRace.addListener(this);
	}
	
	@Override
	public void competitorPositionChanged(GPSFixMoving fix,
			Competitor competitor) {
		// TODO implement me
		// Calculate the bearing b at which the boat will be passing the mark.
		// Passing bearing is the angle bisector of the two legs of the track that meet at the mark plus 180 degrees.
		// Check if boat crossed the passing bearing on its way from previousFix to fix.
		for (Waypoint wp : trackedRace.getRace().getCourse().getWaypoints()) {
			// 1 mark = buoy, 2 marks = gate
			for (Mark m : wp.getMarks()) {
				DynamicGPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(m);
				markTrack.lockForRead();
				Position lastMarkPos = markTrack.getLastRawFix().getPosition();
				Position previousMarkPos = null;
				Bearing bearingToPrev = lastMarkPos.getBearingGreatCircle(previousMarkPos);
				Position nextMarkPos = null;
				Bearing bearingToNext = lastMarkPos.getBearingGreatCircle(nextMarkPos );
				markTrack.unlockAfterRead();
			}
		}
		Iterable<MarkPassing> newMarkPassings = null;
		trackedRace.updateMarkPassings(competitor, newMarkPassings);
	}

	@Override
	public void markPositionChanged(GPSFix fix, Mark mark) {
		// do nothing
	}

	@Override
	public void markPassingReceived(Competitor competitor,
			Map<Waypoint, MarkPassing> oldMarkPassings,
			Iterable<MarkPassing> markPassings) {
		// do nothing
	}

	@Override
	public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage,
			long newMillisecondsOverWhichToAverage) {
		// do nothing
	}

	@Override
	public void windDataReceived(Wind wind, WindSource windSource) {
		// do nothing
	}

	@Override
	public void windDataRemoved(Wind wind, WindSource windSource) {
		// do nothing		
	}

	@Override
	public void windAveragingChanged(long oldMillisecondsOverWhichToAverage,
			long newMillisecondsOverWhichToAverage) {
		// do nothing		
	}

	@Override
	public void raceTimesChanged(TimePoint startOfTracking,
			TimePoint endOfTracking, TimePoint startTimeReceived) {
		// do nothing		
	}

	@Override
	public void delayToLiveChanged(long delayToLiveInMillis) {
		// do nothing		
	}

	@Override
	public void windSourcesToExcludeChanged(
			Iterable<? extends WindSource> windSourcesToExclude) {
		// do nothing		
	}
}
