package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;

/**
 * A template that can be used to construct a {@link ControlPointWithTwoMarks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkPairTemplate extends ControlPointTemplate {
    MarkTemplate getLeft();

    MarkTemplate getRight();
}
