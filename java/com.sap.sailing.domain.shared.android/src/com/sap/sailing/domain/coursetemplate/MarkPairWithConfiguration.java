package com.sap.sailing.domain.coursetemplate;

import java.util.Arrays;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;

/**
 * A template that can be used to construct a {@link ControlPointWithTwoMarks}.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <P>
 *            type of the annotation used to convey positioning-related information; typical instantiations would, e.g.,
 *            be with {@link MarkConfigurationRequestAnnotation} and {@link MarkConfigurationResponseAnnotation}.
 */
public interface MarkPairWithConfiguration<P> extends ControlPointWithMarkConfiguration<P> {
    MarkConfiguration<P> getLeft();

    MarkConfiguration<P> getRight();

    @Override
    default Iterable<MarkConfiguration<P>> getMarkConfigurations() {
        return Arrays.asList(getLeft(), getRight());
    }
}
