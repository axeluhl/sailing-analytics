package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * Holds a single competitor's scoring details for a single race. It may optionally contain
 * a list of {@link LegEntryDTO} objects providing details about the individual legs sailed
 * during the race.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LeaderboardEntryDTO implements Serializable {
    private static final long serialVersionUID = -4678693781217869837L;

    /**
     * Identifies the race in which the competitor achieved this score. This makes it possible to find out in which
     * fleet the competitor started in this column.
     */
    public RaceIdentifier race;
    
    /**
     * Either <code>null</code> in case no max points, or one of "DNS", "DNF", "OCS", "DND", "RAF", "BFD", "DNC", or "DSQ"
     */
    public MaxPointsReason reasonForMaxPoints;
    
    public Double totalPoints;
    public Double totalPointsUncorrected;

    public int trackedRank;
    
    /**
     * Tells if the total points have been overridden by a score correction. Can be used to render differently in editing environment.
     */
    public boolean totalPointsCorrected;
    
    public Double netPoints;
    
    public boolean discarded;
    
    public Double windwardDistanceToCompetitorFarthestAheadInMeters;
    
    public Double averageAbsoluteCrossTrackErrorInMeters;
    
    public Double averageSignedCrossTrackErrorInMeters;
    
    public Double distanceToStartLineFiveSecondsBeforeStartInMeters;
    
    public Double speedOverGroundFiveSecondsBeforeStartInKnots;
    
    public Double distanceToStartLineAtStartOfRaceInMeters;
    
    public Double speedOverGroundAtStartOfRaceInKnots;
    
    public Double speedOverGroundAtPassingStartWaypointInKnots;
    
    public Double timeBetweenRaceStartAndCompetitorStartInSeconds;
    
    public Double distanceToStarboardSideOfStartLineInMeters;
    
    public Tack startTack;
    
    /**
     * If we have GPS data for the competitor for whom this is a leaderboard entry, tells the time since the last
     * non-extrapolated GPS fix that was really received from the tracking device at or before the time point for which
     * the leaderboard was queried. The user interface may---particularly in live mode---choose to visualize the time
     * that passed between the last fix and the query time point for which this entry was created.
     */
    public Double timeSinceLastPositionFixInSeconds;
    
    /**
     * For the competitor's track in the race represented by this object, if a track is present and the track has more
     * than one fix, this field tells the average duration between two fixes on the competitor's track.
     */
    public Duration averageSamplingInterval;
    
    public Double averageRideHeightInMeters;
    
    /**
     * The time gap to the competitor leading the race; for one-design races this is the time the competitor is expected
     * to need to reach the leader's (windward) position (if in the same leg) or the difference between the time at
     * which the competitor is expected to arrive at the next mark and the time when the leader had passed that mark.
     * <p>
     * 
     * For handicap ranking, things are more complicated. All competitors are projected to the boat farthest ahead (the
     * "fastest" boat), using their average VMG in the current leg, and if the fastest boat is no longer in the same
     * leg, using the same handicapped performance as the fastest boat for subsequent legs. This gives an actual arrival
     * time at the fastest boat's current (windward) position, and the corrected time can be computed for all
     * competitors, using the same total windward distance traveled (namely that leading up to the fastest boat's
     * current position). This enables sorting the competitors by these corrected times, yielding a "leader" of the
     * race. This field then describes the actual duration that this competitor would have had to be earlier where she
     * is now in order to rank equal in corrected time with the leader at the fastest boat's current position.
     * <p>
     * 
     * Note that for handicap ranking, this metric can differ from the leg-specific
     * {@link LegEntryDTO#gapToLeaderInSeconds} even for the current leg because the leg's gap metric only considers the
     * race up to the leg's end, regardless of where the leading and fastest boat are.
     */
    public Duration gapToLeaderInOwnTime;

    /**
     * The corrected time spent in the race; usually based on the current time and distance, calculated by the {@link RankingMetric}.
     * For one-design classes this equals the time spent in the race.
     */
    public Duration calculatedTime;
    
    /**
     * The corrections applied to the time and distance sailed when the competitor would have reached the
     * competitor farthest ahead, based on average VMG on the current leg and equal performance to the boat
     * farthest ahead for all subsequent legs.
     */
    public Duration calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead;

    /**
     * If <code>null</code>, no leg details are known yet, the race is not being tracked or the details
     * haven't been requested from the server yet. Otherwise, the list holds one entry per <code>Leg</code> of the
     * <code>Course</code> being sailed in the race for which this object holds the scoring details.
     */
    public List<LegEntryDTO> legDetails;

    /**
     * <code>null</code>, if the fleet couldn't be determined, e.g., because the tracked race isn't known and therefore
     * the link to the fleet is not known; otherwise the description of the fleet in which the competitor scored this
     * entry
     */
    public FleetDTO fleet;

    public LeaderboardEntryDTO() { }
    
    public boolean hasScoreCorrection() {
        return totalPointsCorrected || (reasonForMaxPoints != null && reasonForMaxPoints != MaxPointsReason.NONE);
    }
    
    public Duration getTimeSailed() {
        final Duration result;
        if (legDetails != null) {
            long timeInMilliseconds = 0;
            for (LegEntryDTO legDetail : legDetails) {
                if (legDetail != null) {
                    if (legDetail.distanceTraveledInMeters != null && legDetail.timeInMilliseconds != null) {
                        timeInMilliseconds += legDetail.timeInMilliseconds;
                    } else {
                        timeInMilliseconds = 0;
                        break;
                    }
                }
            }
            result = new MillisecondsDurationImpl(timeInMilliseconds);
        } else {
            result = null;
        }
        return result;
    }
    
    public Distance getDistanceTraveled() {
        Distance result = null;
        if (legDetails != null) {
            for (LegEntryDTO legDetail : legDetails) {
                if (legDetail != null) {
                    if (legDetail.distanceTraveledInMeters != null) {
                        if (result == null) {
                            result = Distance.NULL;
                        }
                        result = result.add(new MeterDistance(legDetail.distanceTraveledInMeters));
                    }
                }
            }
        }
        return result;
    }
    
    public Duration getDurationFoiled() {
        final Duration result;
        if (legDetails != null) {
            Double acc = null;
            for (LegEntryDTO legDetail : legDetails) {
                if (legDetail != null) {
                    if (legDetail.currentDurationFoiledInSeconds != null) {
                        if (acc == null) {
                            acc = 0.0;
                        }
                        acc += legDetail.currentDurationFoiledInSeconds;
                    } else {
                        acc = null;
                        break;
                    }
                }
            }
            result = acc == null ? null : new MillisecondsDurationImpl((long) (acc*1000.));
        } else {
            result = null;
        }
        return result;
    }
    
    public Distance getDistanceFoiled() {
        Distance result = null;
        if (legDetails != null) {
            for (LegEntryDTO legDetail : legDetails) {
                if (legDetail != null) {
                    if (legDetail.currentDistanceFoiledInMeters != null) {
                        if (result == null) {
                            result = Distance.NULL;
                        }
                        result = result.add(new MeterDistance(legDetail.currentDistanceFoiledInMeters));
                    }
                }
            }
        }
        return result;
    }
    
    public int getOneBasedCurrentLegNumber() {
        int result = 0;
        if (legDetails != null && !legDetails.isEmpty()) {
            for (LegEntryDTO legDetail : legDetails) {
                if (legDetail != null && legDetail.started) {
                    result++;
                } else {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((averageAbsoluteCrossTrackErrorInMeters == null) ? 0 : averageAbsoluteCrossTrackErrorInMeters.hashCode());
        result = prime * result
                + ((gapToLeaderInOwnTime == null) ? 0 : gapToLeaderInOwnTime.hashCode());
        result = prime * result
                + ((calculatedTime == null) ? 0 : calculatedTime.hashCode());
        result = prime * result
                + ((calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead == null) ? 0 : calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead.hashCode());
        result = prime * result
                + ((averageSignedCrossTrackErrorInMeters == null) ? 0 : averageSignedCrossTrackErrorInMeters.hashCode());
        result = prime * result + (discarded ? 1231 : 1237);
        result = prime
                * result
                + ((distanceToStarboardSideOfStartLineInMeters == null) ? 0
                        : distanceToStarboardSideOfStartLineInMeters.hashCode());
        result = prime
                * result
                + ((timeBetweenRaceStartAndCompetitorStartInSeconds == null) ? 0
                        : timeBetweenRaceStartAndCompetitorStartInSeconds.hashCode());
        result = prime
                * result
                + ((distanceToStartLineAtStartOfRaceInMeters == null) ? 0 : distanceToStartLineAtStartOfRaceInMeters
                        .hashCode());
        result = prime * result + ((fleet == null) ? 0 : fleet.hashCode());
        result = prime * result + ((legDetails == null) ? 0 : legDetails.hashCode());
        result = prime * result + ((totalPoints == null) ? 0 : totalPoints.hashCode());
        result = prime * result + ((totalPointsUncorrected == null) ? 0 : totalPointsUncorrected.hashCode());
        result = prime * result + (totalPointsCorrected ? 1231 : 1237);
        result = prime * result + ((race == null) ? 0 : race.hashCode());
        result = prime * result + ((reasonForMaxPoints == null) ? 0 : reasonForMaxPoints.hashCode());
        result = prime
                * result
                + ((speedOverGroundAtPassingStartWaypointInKnots == null) ? 0
                        : speedOverGroundAtPassingStartWaypointInKnots.hashCode());
        result = prime * result
                + ((speedOverGroundAtStartOfRaceInKnots == null) ? 0 : speedOverGroundAtStartOfRaceInKnots.hashCode());
        result = prime * result + ((startTack == null) ? 0 : startTack.hashCode());
        result = prime * result + ((averageRideHeightInMeters == null) ? 0 : averageRideHeightInMeters.hashCode());
        result = prime * result + ((averageSamplingInterval == null) ? 0 : averageSamplingInterval.hashCode());
        result = prime * result + ((timeSinceLastPositionFixInSeconds == null) ? 0 : timeSinceLastPositionFixInSeconds.hashCode());
        result = prime * result + ((netPoints == null) ? 0 : netPoints.hashCode());
        result = prime
                * result
                + ((windwardDistanceToCompetitorFarthestAheadInMeters == null) ? 0 : windwardDistanceToCompetitorFarthestAheadInMeters
                        .hashCode());
        result = prime * result + trackedRank;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LeaderboardEntryDTO other = (LeaderboardEntryDTO) obj;
        if (gapToLeaderInOwnTime == null) {
            if (other.gapToLeaderInOwnTime != null)
                return false;
        } else if (!gapToLeaderInOwnTime.equals(other.gapToLeaderInOwnTime))
            return false;
        if (calculatedTime == null) {
            if (other.calculatedTime != null)
                return false;
        } else if (!calculatedTime.equals(other.calculatedTime))
            return false;
        if (calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead == null) {
            if (other.calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead != null)
                return false;
        } else if (!calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead.equals(other.calculatedTimeAtEstimatedArrivalAtCompetitorFarthestAhead))
            return false;
        if (averageAbsoluteCrossTrackErrorInMeters == null) {
            if (other.averageAbsoluteCrossTrackErrorInMeters != null)
                return false;
        } else if (!averageAbsoluteCrossTrackErrorInMeters.equals(other.averageAbsoluteCrossTrackErrorInMeters))
            return false;
        if (averageSignedCrossTrackErrorInMeters == null) {
            if (other.averageSignedCrossTrackErrorInMeters != null)
                return false;
        } else if (!averageSignedCrossTrackErrorInMeters.equals(other.averageSignedCrossTrackErrorInMeters))
            return false;
        if (discarded != other.discarded)
            return false;
        if (distanceToStarboardSideOfStartLineInMeters == null) {
            if (other.distanceToStarboardSideOfStartLineInMeters != null)
                return false;
        } else if (!distanceToStarboardSideOfStartLineInMeters.equals(other.distanceToStarboardSideOfStartLineInMeters))
            return false;
        if (distanceToStartLineAtStartOfRaceInMeters == null) {
            if (other.distanceToStartLineAtStartOfRaceInMeters != null)
                return false;
        } else if (!distanceToStartLineAtStartOfRaceInMeters.equals(other.distanceToStartLineAtStartOfRaceInMeters))
            return false;
        if (timeBetweenRaceStartAndCompetitorStartInSeconds == null) {
            if (other.timeBetweenRaceStartAndCompetitorStartInSeconds != null)
                return false;
        } else if (!timeBetweenRaceStartAndCompetitorStartInSeconds.equals(other.timeBetweenRaceStartAndCompetitorStartInSeconds))
            return false;
        if (timeBetweenRaceStartAndCompetitorStartInSeconds == null) {
            if (other.timeBetweenRaceStartAndCompetitorStartInSeconds != null)
                return false;
        } else if (!timeBetweenRaceStartAndCompetitorStartInSeconds.equals(other.timeBetweenRaceStartAndCompetitorStartInSeconds))
            return false;
        if (fleet == null) {
            if (other.fleet != null)
                return false;
        } else if (!fleet.equals(other.fleet))
            return false;
        if (legDetails == null) {
            if (other.legDetails != null)
                return false;
        } else if (!legDetails.equals(other.legDetails))
            return false;
        if (totalPoints == null) {
            if (other.totalPoints != null)
                return false;
        } else if (!totalPoints.equals(other.totalPoints))
            return false;
        if (totalPointsUncorrected == null) {
            if (other.totalPointsUncorrected != null)
                return false;
        } else if (!totalPointsUncorrected.equals(other.totalPointsUncorrected))
            return false;
        if (totalPointsCorrected != other.totalPointsCorrected)
            return false;
        if (race == null) {
            if (other.race != null)
                return false;
        } else if (!race.equals(other.race))
            return false;
        if (reasonForMaxPoints != other.reasonForMaxPoints)
            return false;
        if (speedOverGroundAtPassingStartWaypointInKnots == null) {
            if (other.speedOverGroundAtPassingStartWaypointInKnots != null)
                return false;
        } else if (!speedOverGroundAtPassingStartWaypointInKnots
                .equals(other.speedOverGroundAtPassingStartWaypointInKnots))
            return false;
        if (speedOverGroundAtStartOfRaceInKnots == null) {
            if (other.speedOverGroundAtStartOfRaceInKnots != null)
                return false;
        } else if (!speedOverGroundAtStartOfRaceInKnots.equals(other.speedOverGroundAtStartOfRaceInKnots))
            return false;
        if (startTack != other.startTack)
            return false;
        if (averageRideHeightInMeters == null) {
            if (other.averageRideHeightInMeters != null)
                return false;
        } else if (!averageRideHeightInMeters.equals(other.averageRideHeightInMeters))
            return false;
        if (averageSamplingInterval == null) {
            if (other.averageSamplingInterval != null)
                return false;
        } else if (!averageSamplingInterval.equals(other.averageSamplingInterval))
            return false;
        if (timeSinceLastPositionFixInSeconds == null) {
            if (other.timeSinceLastPositionFixInSeconds != null)
                return false;
        } else if (!timeSinceLastPositionFixInSeconds.equals(other.timeSinceLastPositionFixInSeconds))
            return false;
        if (netPoints == null) {
            if (other.netPoints != null)
                return false;
        } else if (!netPoints.equals(other.netPoints))
            return false;
        if (windwardDistanceToCompetitorFarthestAheadInMeters == null) {
            if (other.windwardDistanceToCompetitorFarthestAheadInMeters != null)
                return false;
        } else if (!windwardDistanceToCompetitorFarthestAheadInMeters.equals(other.windwardDistanceToCompetitorFarthestAheadInMeters))
            return false;
        if (trackedRank != other.trackedRank)
            return false;
        return true;
    }
    
}
