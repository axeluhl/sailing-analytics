package com.sap.sailing.windestimation;

import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.impl.AllManeuversOfRaceManeuverSequenceGraph;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraphBasedWindEstimatorImpl
        extends AbstractWindEstimatorImpl<CompleteManeuverCurveWithEstimationData> {

    public ManeuverSequenceGraphBasedWindEstimatorImpl(PolarDataService polarService) {
        super(polarService);
    }

    @Override
    protected List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> filteredCompetitorTracks) {
        AllManeuversOfRaceManeuverSequenceGraph maneuverGraph = new AllManeuversOfRaceManeuverSequenceGraph(
                filteredCompetitorTracks, getPolarService());
        return maneuverGraph.estimateWindTrack();
    }

    @Override
    protected void filterOutIrrelevantElementsFromCompetitorTracks(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> filteredCompetitorTracks) {
        for (CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> track : filteredCompetitorTracks) {
            CompleteManeuverCurveWithEstimationData previousManeuver = null, currentManeuver = null;
            for (Iterator<CompleteManeuverCurveWithEstimationData> iterator = track.getElements().iterator(); iterator
                    .hasNext();) {
                CompleteManeuverCurveWithEstimationData nextManeuver = iterator.next();
                if (currentManeuver != null
                        && Math.abs(currentManeuver.getMainCurve().getDirectionChangeInDegrees()) < 30
                        || !currentManeuver.isManeuverClean(previousManeuver, nextManeuver)) {
                    iterator.remove();
                }
                previousManeuver = currentManeuver;
                currentManeuver = nextManeuver;
            }
        }
    }

}
