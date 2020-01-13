package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sse.common.Named;

/**
 * What a {@link WaypointTemplate} references. It is a template for the construction of a {@link ControlPoint} and
 * references the {@link MarkTemplate}s from which this control point shall be constructed.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <P>
 *            type of the annotation used to convey positioning-related information on the {@link MarkConfiguration}
 *            objects contains; typical instantiations would, e.g., be with {@link MarkConfigurationRequestAnnotation} and
 *            {@link MarkConfigurationResponseAnnotation}.
 */
public interface ControlPointWithMarkConfiguration<P> extends Named {
    Iterable<MarkConfiguration<P>> getMarkConfigurations();

    String getShortName();
}
