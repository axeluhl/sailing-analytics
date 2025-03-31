package com.sap.sailing.domain.coursefactory;

import java.util.Map;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sse.common.TimePoint;

/**
 * A repository of {@link MarkProperties} that is available for binding to {@link MarkTemplate}s and thereby configuring
 * {@link Mark}s when creating them from the {@link MarkTemplate}s. Such a repository manages links between
 * {@link MarkTemplate} and {@link MarkProperties} objects whenever a {@link MarkProperties} object is created and/or
 * used to configure the mark created for a {@link MarkTemplate}. The repository can propose {@link MarkProperties}
 * objects to use to configure the mark created from a {@link MarkTemplate}, e.g., based on previous usages.
 * <p>
 * 
 * The repository can create a new {@link MarkProperties} object for a {@link MarkTemplate}. In this case, the link
 * between them will automatically be created and maintained, and the {@link CommonMarkProperties properties for the
 * mark} defined by the {@link MarkTemplate} will provide the initial values for the corresponding
 * {@link MarkProperties} attributes.
 * <p>
 * 
 * This repository may manage a subset of the full set available in the application of all {@link MarkProperties} and
 * their links to zero or more {@link MarkTemplates}. The repository contents may be the result of a filter, e.g., by
 * access permissions or ownership for a user, or by applying filters for, e.g., a venue or a course area.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkPropertiesRepository {
    /**
     * @return all mark properties managed by this repository; this may be a subset of the full set of all
     *         {@link MarkProperties} available in this application, constrained, e.g., by user access permissions
     */
    Iterable<MarkProperties> getAllMarkProperties();
    
    /**
     * Whenever {@link MarkProperties} have been applied to configure a mark that was created from a {@link MarkTemplate}, the
     * application of this object is remembered. With this method, previous applications can be looked up. In addition
     * to the {@link MarkTemplate}s to which these properties were applied, the time point of applying these properties
     * is recorded to allow clients to make default proposals based on a "most recently used" basis.
     */
    Map<MarkProperties, TimePoint> getPreviousUsage(MarkTemplate markTemplate);
}
