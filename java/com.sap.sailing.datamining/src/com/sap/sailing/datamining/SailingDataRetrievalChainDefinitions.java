package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasBravoFixContext;
import com.sap.sailing.datamining.data.HasBravoFixTrackContext;
import com.sap.sailing.datamining.data.HasCompleteManeuverCurveWithEstimationDataContext;
import com.sap.sailing.datamining.data.HasFoilingSegmentContext;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasManeuverSpeedDetailsContext;
import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.data.HasRaceResultOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.data.HasWindFixContext;
import com.sap.sailing.datamining.data.HasWindTrackContext;
import com.sap.sailing.datamining.impl.components.BravoFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.BravoFixTrackRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.CompetitorOfRaceInLeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.CompleteManeuverCurveWithEstimationDataRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.FoilingSegmentRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.ManeuverRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.ManeuverSpeedDetailsRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.MarkPassingRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RaceOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.WindFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.WindTrackRetrievalProcessor;
import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
import com.sap.sailing.datamining.shared.ManeuverSettings;
import com.sap.sailing.datamining.shared.ManeuverSettingsImpl;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettingsImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.SimpleDataRetrieverChainDefinition;

public class SailingDataRetrievalChainDefinitions {

    private final Collection<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions;

    public SailingDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();
        
        final DataRetrieverChainDefinition<RacingEventService, HasLeaderboardContext> leaderboardRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                RacingEventService.class, HasLeaderboardContext.class, "LeaderboardSailingDomainRetrieverChain");
        leaderboardRetrieverChainDefinition.startWith(LeaderboardGroupRetrievalProcessor.class, HasLeaderboardGroupContext.class, "LeaderboardGroup");
        leaderboardRetrieverChainDefinition.endWith(LeaderboardGroupRetrievalProcessor.class, LeaderboardRetrievalProcessor.class,
                HasLeaderboardContext.class, "Leaderboard");
        
        final DataRetrieverChainDefinition<RacingEventService, HasRaceResultOfCompetitorContext> raceResultOfCompetitorRetrieverChainDefinition =
                new SimpleDataRetrieverChainDefinition<>(leaderboardRetrieverChainDefinition, HasRaceResultOfCompetitorContext.class, "RaceResultSailingDomainRetrieverChain");
        raceResultOfCompetitorRetrieverChainDefinition.endWith(LeaderboardRetrievalProcessor.class, CompetitorOfRaceInLeaderboardRetrievalProcessor.class, HasRaceResultOfCompetitorContext.class, "Competitor");
        dataRetrieverChainDefinitions.add(raceResultOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasTrackedRaceContext> trackedRaceRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                leaderboardRetrieverChainDefinition, HasTrackedRaceContext.class, "RaceSailingDomainRetrieverChain");
        trackedRaceRetrieverChainDefinition.endWith(LeaderboardRetrievalProcessor.class, TrackedRaceRetrievalProcessor.class,
                HasTrackedRaceContext.class, "Race");
        dataRetrieverChainDefinitions.add(trackedRaceRetrieverChainDefinition);
        
        final DataRetrieverChainDefinition<RacingEventService, HasRaceOfCompetitorContext> raceOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasRaceOfCompetitorContext.class, "RaceOfCompetitorSailingDomainRetrieverChain");
        raceOfCompetitorRetrieverChainDefinition.endWith(TrackedRaceRetrievalProcessor.class, RaceOfCompetitorRetrievalProcessor.class, HasRaceOfCompetitorContext.class, "Competitor");
        dataRetrieverChainDefinitions.add(raceOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasBravoFixTrackContext> bravoFixTrackRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                raceOfCompetitorRetrieverChainDefinition, HasBravoFixTrackContext.class, "BravoFixTrackSailingDomainRetrieverChain");
        bravoFixTrackRetrieverChainDefinition.endWith(RaceOfCompetitorRetrievalProcessor.class, BravoFixTrackRetrievalProcessor.class,
                HasBravoFixTrackContext.class, "BravoFixTrack");
        dataRetrieverChainDefinitions.add(bravoFixTrackRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasFoilingSegmentContext> foilingSegmentsRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                raceOfCompetitorRetrieverChainDefinition, HasFoilingSegmentContext.class, "FoilingSegmentsSailingDomainRetrieverChain");
        foilingSegmentsRetrieverChainDefinition.endWith(RaceOfCompetitorRetrievalProcessor.class, FoilingSegmentRetrievalProcessor.class,
                HasFoilingSegmentContext.class, FoilingSegmentsDataMiningSettings.class, FoilingSegmentsDataMiningSettings.createDefaultSettings(), "FoilingSegments");
        dataRetrieverChainDefinitions.add(foilingSegmentsRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasTrackedLegOfCompetitorContext> legOfCompetitorRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasTrackedLegOfCompetitorContext.class, "LegSailingDomainRetrieverChain");
        legOfCompetitorRetrieverChainDefinition.addAfter(TrackedRaceRetrievalProcessor.class, TrackedLegRetrievalProcessor.class, HasTrackedLegContext.class, "Leg");
        legOfCompetitorRetrieverChainDefinition.endWith(TrackedLegRetrievalProcessor.class, TrackedLegOfCompetitorRetrievalProcessor.class,
                HasTrackedLegOfCompetitorContext.class, "LegOfCompetitor");
        dataRetrieverChainDefinitions.add(legOfCompetitorRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasGPSFixContext> gpsFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasGPSFixContext.class, "GPSFixSailingDomainRetrieverChain");
        gpsFixRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, GPSFixRetrievalProcessor.class,
                HasGPSFixContext.class, "GpsFix");
        dataRetrieverChainDefinitions.add(gpsFixRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasBravoFixContext> bravoFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasBravoFixContext.class, "BravoFixSailingDomainRetrieverChain");
        bravoFixRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, BravoFixRetrievalProcessor.class,
                HasBravoFixContext.class, "BravoFix");
        dataRetrieverChainDefinitions.add(bravoFixRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasWindFixContext> windFixRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                trackedRaceRetrieverChainDefinition, HasWindFixContext.class, "WindFixSailingDomainRetrieverChain");
        windFixRetrieverChainDefinition.addAfter(TrackedRaceRetrievalProcessor.class, WindTrackRetrievalProcessor.class, HasWindTrackContext.class, "WindTrack");
        windFixRetrieverChainDefinition.endWith(WindTrackRetrievalProcessor.class, WindFixRetrievalProcessor.class,
                HasWindFixContext.class, "WindFix");
        dataRetrieverChainDefinitions.add(windFixRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasManeuverContext> maneuverRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasManeuverContext.class, "ManeuverSailingDomainRetrieverChain");
        maneuverRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, ManeuverRetrievalProcessor.class,
                HasManeuverContext.class, ManeuverSettings.class, ManeuverSettingsImpl.createDefault(), "Maneuver");
        dataRetrieverChainDefinitions.add(maneuverRetrieverChainDefinition);
        
        DataRetrieverChainDefinition<RacingEventService, HasManeuverSpeedDetailsContext> speedDetailsDataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                maneuverRetrieverChainDefinition, HasManeuverSpeedDetailsContext.class,
                "ManeuverSpeedDetailsRetrieverChain");
        speedDetailsDataRetrieverChainDefinition.endWith(ManeuverRetrievalProcessor.class,
                ManeuverSpeedDetailsRetrievalProcessor.class, HasManeuverSpeedDetailsContext.class,
                ManeuverSpeedDetailsSettings.class, ManeuverSpeedDetailsSettingsImpl.createDefault(),
                "ManeuverSpeedDetails");
        dataRetrieverChainDefinitions.add(speedDetailsDataRetrieverChainDefinition);
        
        DataRetrieverChainDefinition<RacingEventService, HasCompleteManeuverCurveWithEstimationDataContext> completeManeuverCurveWithEstimationDataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                raceOfCompetitorRetrieverChainDefinition, HasCompleteManeuverCurveWithEstimationDataContext.class,
                "CompleteManeuverCurveWithEstimationDataRetrieverChain");
        completeManeuverCurveWithEstimationDataRetrieverChainDefinition.endWith(RaceOfCompetitorRetrievalProcessor.class,
                CompleteManeuverCurveWithEstimationDataRetrievalProcessor.class, HasCompleteManeuverCurveWithEstimationDataContext.class, ManeuverSettings.class, ManeuverSettingsImpl.createDefault(),
                "CompleteManeuverCurveWithEstimationData");
        dataRetrieverChainDefinitions.add(completeManeuverCurveWithEstimationDataRetrieverChainDefinition);

        final DataRetrieverChainDefinition<RacingEventService, HasMarkPassingContext> markPassingRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(
                legOfCompetitorRetrieverChainDefinition, HasMarkPassingContext.class, "MarkPassingSailingDomainRetrieverChain");
        markPassingRetrieverChainDefinition.endWith(TrackedLegOfCompetitorRetrievalProcessor.class, MarkPassingRetrievalProcessor.class, HasMarkPassingContext.class, "MarkPassing");
        dataRetrieverChainDefinitions.add(markPassingRetrieverChainDefinition);
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> get() {
        return dataRetrieverChainDefinitions;
    }

}
