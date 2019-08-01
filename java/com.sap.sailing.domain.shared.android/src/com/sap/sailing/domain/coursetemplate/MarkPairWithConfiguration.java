package com.sap.sailing.domain.coursetemplate;

import java.util.Arrays;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;

/**
 * A template that can be used to construct a {@link ControlPointWithTwoMarks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkPairWithConfiguration extends ControlPointWithMarkConfiguration {
    MarkConfiguration getLeft();

    MarkConfiguration getRight();

    @Override
    default Iterable<MarkConfiguration> getMarkMappings() {
        return Arrays.asList(getLeft(), getRight());
    }
}
