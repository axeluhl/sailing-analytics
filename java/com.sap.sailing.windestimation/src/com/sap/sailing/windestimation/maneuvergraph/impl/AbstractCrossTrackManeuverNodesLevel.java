package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractCrossTrackManeuverNodesLevel<SelfType extends AbstractCrossTrackManeuverNodesLevel<SelfType>>
        extends AbstractManeuverNodesLevel<SelfType> {

    private final SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel;

    protected boolean calculationOfTransitionProbabilitiesNeeded = true;

    public AbstractCrossTrackManeuverNodesLevel(SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
        super(singleTrackManeuverNodesLevel.getManeuver());
        this.singleTrackManeuverNodesLevel = singleTrackManeuverNodesLevel;
    }

    public SingleTrackManeuverNodesLevel getSingleTrackManeuverNodesLevel() {
        return singleTrackManeuverNodesLevel;
    }

    @Override
    public BoatClass getBoatClass() {
        return singleTrackManeuverNodesLevel.getBoatClass();
    }

    @Override
    public boolean isManeuverBeginningClean() {
        return singleTrackManeuverNodesLevel.isManeuverBeginningClean();
    }

    @Override
    public boolean isManeuverEndClean() {
        return singleTrackManeuverNodesLevel.isManeuverEndClean();
    }

    @Override
    public void setTackProbabilityBonusToManeuver(double tackProbabilityBonus) {
        singleTrackManeuverNodesLevel.setTackProbabilityBonusToManeuver(tackProbabilityBonus);
        if (singleTrackManeuverNodesLevel.isCalculationOfTransitionProbabilitiesNeeded()) {
            this.calculationOfTransitionProbabilitiesNeeded = true;
            SingleTrackManeuverNodesLevel thisLevelPreviousSingleTrackLevel = singleTrackManeuverNodesLevel
                    .getPreviousLevel();
            if (thisLevelPreviousSingleTrackLevel != null) {
                SelfType matchedCrossTrackManeuverNodesLevel = getPreviousLevel();
                while (matchedCrossTrackManeuverNodesLevel != null
                        && thisLevelPreviousSingleTrackLevel != matchedCrossTrackManeuverNodesLevel
                                .getSingleTrackManeuverNodesLevel()) {
                    matchedCrossTrackManeuverNodesLevel = matchedCrossTrackManeuverNodesLevel.getPreviousLevel();
                }
                if (matchedCrossTrackManeuverNodesLevel != null) {
                    matchedCrossTrackManeuverNodesLevel.calculationOfTransitionProbabilitiesNeeded = true;
                }
            }
        }
    }

    @Override
    public boolean isCalculationOfTransitionProbabilitiesNeeded() {
        return calculationOfTransitionProbabilitiesNeeded;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getPreviousManeuverOfSameTrack() {
        return singleTrackManeuverNodesLevel.getPreviousManeuverOfSameTrack();
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getNextManeuverOfSameTrack() {
        return singleTrackManeuverNodesLevel.getNextManeuverOfSameTrack();
    }

}
