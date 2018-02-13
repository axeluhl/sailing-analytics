package com.sap.sailing.gwt.ui.leaderboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.leaderboard.DetailTypeColumn.LegDetailField;

/**
 * Displays competitor's rank in leg and makes the column sortable by rank. The leg is
 * identified as an index into the {@link LeaderboardEntryDTO#legDetails} list.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LegColumn extends ExpandableSortableColumn<String> {
    private final String raceColumnName;
    private final int legIndex;
    private final StringMessages stringMessages;
    private final String headerStyle;
    private final String columnStyle;
    
    private abstract class AbstractLegDetailField<T extends Comparable<?>> implements LegDetailField<T> {
        public T get(LeaderboardRowDTO row) {
            LegEntryDTO entry = getLegEntry(row);
            if (entry == null) {
                return null;
            } else {
                return getFromNonNullEntry(entry);
            }
        }

        protected abstract T getFromNonNullEntry(LegEntryDTO entry);
    }
    
    private class DistanceTraveledInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.distanceTraveledInMeters;
        }
    }
    
    private class DistanceTraveledIncludingGateStartInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.distanceTraveledIncludingGateStartInMeters;
        }
    }
    
    private class TimeTraveledInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return new Long(entry.timeInMilliseconds / 1000).doubleValue();
        }
    }
    
    private class CorrectedTimeTraveledInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.correctedTotalTime == null ? null : entry.correctedTotalTime.asSeconds();
        }
    }
    
    private class AverageSpeedOverGroundInKnots extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.averageSpeedOverGroundInKnots;
        }
    }
    
    private class AverageAbsoluteCrossTrackErrorInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.averageAbsoluteCrossTrackErrorInMeters;
        }
    }
    
    private class AverageSignedCrossTrackErrorInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.averageSignedCrossTrackErrorInMeters;
        }
    }
    
    private class CurrentSpeedOverGroundInKnots extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.currentSpeedOverGroundInKnots;
        }
    }
    
    private class CurrentHeelInDegrees extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.currentHeelInDegrees;
        }
    }

    private class CurrentPitchInDegrees extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.currentPitchInDegrees;
        }
    }

    private class CurrentRideHeightInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.currentRideHeightInMeters;
        }
    }
    
    private class CurrentDistanceFoiledInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.currentDistanceFoiledInMeters;
        }
    }
    
    private class CurrentDurationFoiled extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.currentDurationFoiledInSeconds;
        }
    }
    
    private class EstimatedTimeToNextWaypointInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.estimatedTimeToNextWaypointInSeconds;
        }
    }
    
    private class GapToLeaderInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.gapToLeaderInSeconds;
        }
    }
    
    private class GapChangeSinceLegStartInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.gapChangeSinceLegStartInSeconds;
        }
    }
    
    private class SideToWhichMarkAtLegStartWasRounded extends AbstractLegDetailField<NauticalSide> {
        @Override
        protected NauticalSide getFromNonNullEntry(LegEntryDTO entry) {
            return entry.sideToWhichMarkAtLegStartWasRounded;
        }
    }
    
    private class VelocityMadeGoodInKnots extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.velocityMadeGoodInKnots;
        }
    }
    
    private class WindwardDistanceToGoInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDTO entry) {
            return entry.windwardDistanceToGoInMeters;
        }
    }
    
    private class RankGain implements LegDetailField<Integer> {
        @Override
        public Integer get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            if (legEntry == null || getLegIndex() == 0) {
                // no gain/loss for first leg
                return null;
            } else {
                LegEntryDTO previousEntry = getLegEntry(row, getLegIndex()-1);
                return previousEntry == null ? null : legEntry.rank - previousEntry.rank;
            }
        }
    }
    
    private class ManeuverCountLegDetailsColumn extends FormattedDoubleDetailTypeColumn {
        public ManeuverCountLegDetailsColumn(String headerStyle, String columnStyle) {
            super(DetailType.NUMBER_OF_MANEUVERS, null, headerStyle, columnStyle, getLeaderboardPanel());
        }
        
        @Override
        protected String getTitle(LeaderboardRowDTO row) {
            String resultString = null;
            LegEntryDTO entry = getLegEntry(row);
            if (entry != null && entry.numberOfManeuvers != null) {
                StringBuilder result = new StringBuilder();
                if (entry.numberOfManeuvers.get(ManeuverType.TACK) != null) {
                    result.append(entry.numberOfManeuvers.get(ManeuverType.TACK));
                    result.append(" ");
                    result.append(stringMessages.tacks());
                }
                if (entry.numberOfManeuvers.get(ManeuverType.JIBE) != null) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(entry.numberOfManeuvers.get(ManeuverType.JIBE));
                    result.append(" ");
                    result.append(stringMessages.jibes());
                }
                if (entry.numberOfManeuvers.get(ManeuverType.PENALTY_CIRCLE) != null) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(entry.numberOfManeuvers.get(ManeuverType.PENALTY_CIRCLE));
                    result.append(" ");
                    result.append(stringMessages.penaltyCircles());
                }
                resultString = result.toString();
            }
            return resultString;
        }

        
        @Override
        public String getValue(LeaderboardRowDTO row) {
            Double fieldValue = getFieldValue(row);
            StringBuilder result = new StringBuilder();
            if (fieldValue != null) {
                result.append(getFormatter().format(fieldValue));
            }
            LegEntryDTO entry = getLegEntry(row);
            if (entry != null && entry.numberOfManeuvers != null &&
                    entry.numberOfManeuvers.get(ManeuverType.PENALTY_CIRCLE) != null && (int) entry.numberOfManeuvers.get(ManeuverType.PENALTY_CIRCLE) != 0) {
                result.append(" (");
                result.append(entry.numberOfManeuvers.get(ManeuverType.PENALTY_CIRCLE));
                result.append("P)");
            }
            return result.toString();
        }

        @Override
        protected Double getFieldValue(LeaderboardRowDTO row) {
            LegEntryDTO entry = getLegEntry(row);
            Double result = null;
            if (entry != null) {
                for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE,
                        ManeuverType.PENALTY_CIRCLE }) {
                    if (entry.numberOfManeuvers != null && entry.numberOfManeuvers.get(maneuverType) != null) {
                        if (result == null) {
                            result = (double) entry.numberOfManeuvers.get(maneuverType);
                        } else {
                            result += (double) entry.numberOfManeuvers.get(maneuverType);
                        }
                    }
                }
            }
            return result;
        }
        

    }
        
    private class LegAWA implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionAWA;
        }
    }
    
    private class LegAWS implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionAWS;
        }
    }

    private class LegTWA implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTWA;
        }
    }

    private class LegTWS implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTWS;
        }
    }

    private class LegTWD implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTWD;
        }
    }

    private class LegTargetTWA implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTargTWA;
        }
    }

    private class LegBoatSpeed implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionBoatSpeed;
        }
    }

    private class LegTargetBoatSpeed implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTargBoatSpeed;
        }
    }

    private class LegSOG implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionSOG;
        }
    }

    private class LegCOG implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionCOG;
        }
    }

    private class LegForestayLoad implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionForestayLoad;
        }
    }

    private class LegRake implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionRake;
        }
    }

    private class LegCourse implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionCourseDetail;
        }
    }

    private class LegHeading implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionHeading;
        }
    }

    private class LegVMG implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionVMG;
        }
    }

    private class LegVMGDelta implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionVMGTargVMGDelta;
        }
    }

    private class LegRateOfTurn implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionRateOfTurn;
        }
    }

    private class LegRudderAngle implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionRudderAngle;
        }
    }

    private class LegHeel implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionHeel;
        }
    }

    private class LegTargetHeel implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTargetHeel;
        }
    }

    private class LegTimeToPortLayline implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToPortLayline;
        }
    }

    private class LegTimeToStarboardLayline implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToStbLayline;
        }
    }

    private class LegDistanceToPortLayline implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionDistToPortLayline;
        }
    }

    private class LegDistanceToStarboardLayline implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionDistToStbLayline;
        }
    }

    private class LegTimeToGun implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToGUN.asSeconds();
        }
    }

    private class LegTimeToCommitteeBoat implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToCommitteeBoat;
        }
    }

    private class LegTimeToPin implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToPin;
        }
    }

    private class LegTimeToBurnToLine implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToBurnToLine.asSeconds();
        }
    }

    private class LegTimeToBurnToCommitteeBoat implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToBurnToCommitteeBoat;
        }
    }

    private class LegTimeToBurnToPin implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionTimeToBurnToPin;
        }
    }

    private class LegDistanceToCommitteeBoat implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionDistanceToCommitteeBoat;
        }
    }

    private class LegDistanceToPin implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionDistanceToPinDetail;
        }
    }

    private class LegDistanceBelowLine implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionDistanceBelowLine;
        }
    }

    private class LegLineSquareForWindDirection implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            LegEntryDTO legEntry = getLegEntry(row);
            return legEntry == null ? null : legEntry.expeditionLineSquareForWindDirection;
        }
    }
    
    public LegColumn(LeaderboardPanel<?> leaderboardPanel, String raceColumnName, int legIndex, SortingOrder preferredSortingOrder, StringMessages stringMessages,
            List<DetailType> legDetailSelection, String headerStyle, String columnStyle,
            String detailHeaderStyle, String detailColumnStyle) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), preferredSortingOrder,
                stringMessages, detailHeaderStyle, detailColumnStyle, legDetailSelection, leaderboardPanel);
        setHorizontalAlignment(ALIGN_CENTER);
        this.raceColumnName = raceColumnName;
        this.legIndex = legIndex;
        this.stringMessages = stringMessages;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStyle;
    }

    @Override
    protected Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDetailColumnMap(
            LeaderboardPanel<?> leaderboardPanel, StringMessages stringMessages, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result = new HashMap<>();
        result.put(DetailType.LEG_DISTANCE_TRAVELED,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_DISTANCE_TRAVELED, new DistanceTraveledInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_DISTANCE_TRAVELED_INCLUDING_GATE_START,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_DISTANCE_TRAVELED_INCLUDING_GATE_START, new DistanceTraveledIncludingGateStartInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, 
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, new AverageSpeedOverGroundInKnots(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_CURRENT_SPEED_OVER_GROUND_IN_KNOTS, 
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_CURRENT_SPEED_OVER_GROUND_IN_KNOTS, new CurrentSpeedOverGroundInKnots(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));

        result.put(DetailType.BRAVO_LEG_CURRENT_HEEL_IN_DEGREES, new HeelColumn(DetailType.BRAVO_LEG_CURRENT_HEEL_IN_DEGREES,
                new CurrentHeelInDegrees(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.BRAVO_LEG_CURRENT_PITCH_IN_DEGREES, new PitchColumn(DetailType.BRAVO_LEG_CURRENT_PITCH_IN_DEGREES,
                new CurrentPitchInDegrees(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.BRAVO_LEG_CURRENT_RIDE_HEIGHT_IN_METERS, new RideHeightColumn(DetailType.BRAVO_LEG_CURRENT_RIDE_HEIGHT_IN_METERS,
                new CurrentRideHeightInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.BRAVOEXTENDED_LEG_CURRENT_DISTANCE_FOILED_IN_METERS, new FormattedDoubleDetailTypeColumn(DetailType.BRAVOEXTENDED_LEG_CURRENT_DISTANCE_FOILED_IN_METERS,
                new CurrentDistanceFoiledInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.BRAVOEXTENDED_LEG_CURRENT_DURATION_FOILED_IN_SECONDS, new TotalTimeColumn(DetailType.BRAVOEXTENDED_LEG_CURRENT_DURATION_FOILED_IN_SECONDS,
                new CurrentDurationFoiled(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS, new EstimatedTimeToNextWaypointInSeconds(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_GAP_TO_LEADER_IN_SECONDS,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_GAP_TO_LEADER_IN_SECONDS, new GapToLeaderInSeconds(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_GAP_CHANGE_SINCE_LEG_START_IN_SECONDS,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_GAP_CHANGE_SINCE_LEG_START_IN_SECONDS, new GapChangeSinceLegStartInSeconds(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED,
                new SideToWhichMarkAtLegStartWasRoundedColumn(stringMessages.sideToWhichMarkAtLegStartWasRounded(),
                        new SideToWhichMarkAtLegStartWasRounded(), detailHeaderStyle, detailColumnStyle, stringMessages, leaderboardPanel));
        result.put(DetailType.LEG_VELOCITY_MADE_GOOD_IN_KNOTS,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_VELOCITY_MADE_GOOD_IN_KNOTS, new VelocityMadeGoodInKnots(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_WINDWARD_DISTANCE_TO_GO_IN_METERS, 
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_WINDWARD_DISTANCE_TO_GO_IN_METERS, new WindwardDistanceToGoInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_RANK_GAIN, new RankGainColumn(stringMessages.rankGain(), new RankGain(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.NUMBER_OF_MANEUVERS, new ManeuverCountLegDetailsColumn(detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.LEG_TIME_TRAVELED, new TotalTimeColumn(DetailType.LEG_TIME_TRAVELED, new TimeTraveledInSeconds(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_CORRECTED_TIME_TRAVELED,
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_CORRECTED_TIME_TRAVELED, new CorrectedTimeTraveledInSeconds(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS, 
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR_IN_METERS, new AverageAbsoluteCrossTrackErrorInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.LEG_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS, 
                new FormattedDoubleDetailTypeColumn(DetailType.LEG_AVERAGE_SIGNED_CROSS_TRACK_ERROR_IN_METERS, new AverageSignedCrossTrackErrorInMeters(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_AWA, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_AWA, new LegAWA(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_AWS, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_AWS, new LegAWS(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TWA, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TWA, new LegTWA(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TWS, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TWS, new LegTWS(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TWD, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TWD, new LegTWD(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TARG_TWA, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TARG_TWA, new LegTargetTWA(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_BOAT_SPEED, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_BOAT_SPEED, new LegBoatSpeed(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TARG_BOAT_SPEED, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TARG_BOAT_SPEED, new LegTargetBoatSpeed(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_SOG, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_SOG, new LegSOG(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_COG, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_COG, new LegCOG(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_FORESTAY_LOAD, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_FORESTAY_LOAD, new LegForestayLoad(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_RAKE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_RAKE, new LegRake(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_COURSE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_COURSE, new LegCourse(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_HEADING, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_HEADING, new LegHeading(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_VMG, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_VMG, new LegVMG(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_VMG_TARG_VMG_DELTA, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_VMG_TARG_VMG_DELTA, new LegVMGDelta(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_RATE_OF_TURN, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_RATE_OF_TURN, new LegRateOfTurn(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_RUDDER_ANGLE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_RUDDER_ANGLE, new LegRudderAngle(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_HEEL, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_HEEL, new LegHeel(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TARGET_HEEL, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TARGET_HEEL, new LegTargetHeel(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_PORT_LAYLINE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_PORT_LAYLINE, new LegTimeToPortLayline(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_STB_LAYLINE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_STB_LAYLINE, new LegTimeToStarboardLayline(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_DIST_TO_PORT_LAYLINE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_DIST_TO_PORT_LAYLINE, new LegDistanceToPortLayline(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_DIST_TO_STB_LAYLINE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_DIST_TO_STB_LAYLINE, new LegDistanceToStarboardLayline(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_GUN, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_GUN, new LegTimeToGun(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_COMMITTEE_BOAT, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_COMMITTEE_BOAT, new LegTimeToCommitteeBoat(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_PIN, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_PIN, new LegTimeToPin(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_BURN_TO_LINE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_BURN_TO_LINE, new LegTimeToBurnToLine(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_BURN_TO_COMMITTEE_BOAT, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_BURN_TO_COMMITTEE_BOAT, new LegTimeToBurnToCommitteeBoat(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_TIME_TO_BURN_TO_PIN, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_TIME_TO_BURN_TO_PIN, new LegTimeToBurnToPin(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_DISTANCE_TO_COMMITTEE_BOAT, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_DISTANCE_TO_COMMITTEE_BOAT, new LegDistanceToCommitteeBoat(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_DISTANCE_TO_PIN, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_DISTANCE_TO_PIN, new LegDistanceToPin(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_DISTANCE_BELOW_LINE, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_DISTANCE_BELOW_LINE, new LegDistanceBelowLine(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.EXPEDITION_LEG_LINE_SQUARE_FOR_WIND_DIRECTION, 
                new FormattedDoubleDetailTypeColumn(DetailType.EXPEDITION_LEG_LINE_SQUARE_FOR_WIND_DIRECTION, new LegLineSquareForWindDirection(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        return result;
    }

    private int getLegIndex() {
        return legIndex;
    }
    
    @Override
    public String getColumnStyle() {
        return columnStyle;
    }
    
    @Override
    public String getHeaderStyle() {
        return headerStyle;
    }

    private String getRaceName() {
        return raceColumnName;
    }

    private LegEntryDTO getLegEntry(LeaderboardRowDTO row) {
        int theLegIndex = getLegIndex();
        return getLegEntry(row, theLegIndex);
    }

    private LegEntryDTO getLegEntry(LeaderboardRowDTO row, int theLegIndex) {
        LegEntryDTO legEntry = null;
        LeaderboardEntryDTO entry = row.fieldsByRaceColumnName.get(getRaceName());
        if (entry != null && entry.legDetails != null && entry.legDetails.size() > theLegIndex) {
            legEntry = entry.legDetails.get(theLegIndex);
        }
        return legEntry;
    }
    
    @Override
    public InvertibleComparator<LeaderboardRowDTO> getComparator() {
        return new InvertibleComparatorAdapter<LeaderboardRowDTO>(getPreferredSortingOrder().isAscending()) {
            @Override
            public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                LegEntryDTO o1Entry = getLegEntry(o1);
                LegEntryDTO o2Entry = getLegEntry(o2);
                return o1Entry == null ? o2Entry == null ? 0 : isAscending()?1:-1
                                       : o2Entry == null ? isAscending()?-1:1 : o1Entry.rank - o2Entry.rank;
            }
        };
    }

    @Override
    public SortableExpandableColumnHeader getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(/* title */ stringMessages.leg()+(legIndex+1),
                /* iconURL */ null, getLeaderboardPanel(), this, stringMessages);
        return result;
    }
    
    @Override
    public String getValue(LeaderboardRowDTO row) {
        LeaderboardEntryDTO leaderboardEntryDTO = row.fieldsByRaceColumnName.get(raceColumnName);
        LegEntryDTO legEntry = getLegEntry(row);
        if (legEntry != null && legEntry.rank != 0) {
            return ""+legEntry.rank;
        }  else if (leaderboardEntryDTO.legDetails != null && legIndex+1 > leaderboardEntryDTO.legDetails.size()) {
            return "n/a";
        } else {
            return "";
        }
    }
    
}

