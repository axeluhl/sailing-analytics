package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;
import com.sap.sse.common.NamedWithID;

/**
 * Stores properties that can be applied to a mark in the context of an event or a regatta,
 * including the mark's own attributes as well as tracking-related information such as a
 * reference to the tracking device used to track the mark, or a fixed mark position, such as
 * for a land mark, an official lateral or cardinal buoy, a regatta mark in a fixed position
 * or a lighthouse.<p>
 * 
 * Such a properties object can be linked to zero or more {@link MarkTemplate}s. When the user
 * creates a course from a {@link CourseTemplate}, linked {@link MarkProperties} can be offered
 * to configure the {@link Mark} created from the {@link CourseTemplate}'s {@link MarkTemplate}s.<p>
 * 
 * {@link MarkProperties} objects can be tagged, and the tags may be used to search and filter a
 * library of such {@link MarkProperties}. The tags can, e.g., express a venue name or a course
 * area name or the name of a set of tracking devices typically used to track a course. A user may
 * have more than one {@link MarkProperties} object available that is linked to a particular
 * {@link MarkTemplate}. One of those could be selected depending on when it was used last for
 * the {@link MarkTemplate}, or the user may be presented with the set of {@link MarkProperties}
 * available and can select, search and filter.
 *  
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkProperties extends NamedWithID {
    Color getColor();
    String getShape();
    String getPattern();
    MarkType getType();
    String getShortName();
}
