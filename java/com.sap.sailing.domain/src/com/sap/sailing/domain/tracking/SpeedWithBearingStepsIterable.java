package com.sap.sailing.domain.tracking;

import java.util.Iterator;

/**
 * Encapsulates an {@link Iterable} of {@link #SpeedWithBearingStep} in order to define following specification of the
 * underlying list of speed with bearing steps:
 * <ol>
 * <li>The {@code courseChangeInDegrees} attribute is calculated as the course change between the course of the previous
 * step and current step</li>
 * <li>First step has zero as its {@code courseChangeInDegrees} value, because it does not have a predecessor step.</li>
 * <li>The sum of {@code courseChangeInDegrees} values of all steps calculates the total course change between the time
 * point of the first and last step in this list.</li>
 * </ol>
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SpeedWithBearingStepsIterable implements Iterable<SpeedWithBearingStep> {

    private final Iterable<SpeedWithBearingStep> steps;

    public SpeedWithBearingStepsIterable(Iterable<SpeedWithBearingStep> steps) {
        this.steps = steps;
    }

    @Override
    public Iterator<SpeedWithBearingStep> iterator() {
        return steps.iterator();
    }

}
