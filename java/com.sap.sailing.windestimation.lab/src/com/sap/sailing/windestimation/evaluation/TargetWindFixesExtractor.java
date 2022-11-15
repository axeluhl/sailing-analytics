package com.sap.sailing.windestimation.evaluation;

import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

public interface TargetWindFixesExtractor<T> {

    List<Wind> extractTargetWindFixes(CompetitorTrackWithEstimationData<T> competitorTrack);

}
