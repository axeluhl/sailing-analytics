package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class MarkPassingCalculator {

	AbstractCandidateFinder finder = new CandidateFinder();
	AbstractCandidateChooser chooser = new CandidateChooser();

	public LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> calculateMarkpasses(
			LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> wayPointTracks,
			LinkedHashMap<Competitor, ArrayList<GPSFixMoving>> competitorTracks,
			TimePoint startOfRace, TimePoint endOfRace) {

		LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>> calculatedMarkpasses = new LinkedHashMap<Competitor, LinkedHashMap<Waypoint, MarkPassing>>();

		for (Competitor c : competitorTracks.keySet()) {

			LinkedHashMap<Waypoint, MarkPassing> computedPasses = new LinkedHashMap<Waypoint, MarkPassing>();
			if (!(competitorTracks.get(c).size() == 0)) {

				// Find GPSFix-Candidates for each ControlPoint
				LinkedHashMap<Waypoint, LinkedHashMap<GPSFixMoving, Double>> waypointCandidates = finder
						.findCandidates(competitorTracks.get(c), wayPointTracks);

				// Create "Candidates"
				ArrayList<Candidate> candidates = new ArrayList<Candidate>();

				Candidate start = new Candidate(0, startOfRace.minus(120000), 0);
				candidates.add(start);
				Candidate end = new Candidate(
						wayPointTracks.keySet().size() + 1,
						endOfRace.plus(1800000), 0);
				for (Waypoint w : waypointCandidates.keySet()) {
					for (GPSFixMoving gps : waypointCandidates.get(w).keySet()) {
						Candidate ca = new Candidate((Integer) w.getId(),
								gps.getTimePoint(), waypointCandidates.get(w)
										.get(gps));
						candidates.add(ca);
					}
				}
				candidates.add(end);

				// Find shortest Path and create calculated MarkPasses
				ArrayList<TimePoint> markPasses = chooser.getMarkPasses(
						candidates, start, end);

				Iterator<Waypoint> it = wayPointTracks.keySet().iterator();
				for (int i = markPasses.size() - 1; i >= 0; i--) {
					Waypoint w = it.next();
					computedPasses.put(w, new MarkPassingImpl(
							markPasses.get(i), w, c));
				}
			} else {
				// System.out.println("Competitor has no GPSFixes");
			}
			calculatedMarkpasses.put(c, computedPasses);
		}
		return calculatedMarkpasses;
	}
}