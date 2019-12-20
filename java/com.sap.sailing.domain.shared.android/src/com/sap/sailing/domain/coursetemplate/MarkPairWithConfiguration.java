package com.sap.sailing.domain.coursetemplate;

import java.util.Arrays;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;

/**
 * A template that can be used to construct a {@link ControlPointWithTwoMarks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkPairWithConfiguration<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        extends ControlPointWithMarkConfiguration<MarkConfigurationT> {
    MarkConfigurationT getLeft();

    MarkConfigurationT getRight();

    @Override
    default Iterable<MarkConfigurationT> getMarkConfigurations() {
        return Arrays.asList(getLeft(), getRight());
    }
}
