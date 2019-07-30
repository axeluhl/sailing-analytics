package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Mark;

/**
 * A template for creating a {@link Mark}. It has a globally unique ID and can be used in zero or more
 * {@link CourseTemplate}s. It is immutable. In addition to a {@link Mark}'s properties it offers an optional
 * {@link #getShortName() short name}. Being a special {@link ControlPointTemplate}, a {@link MarkTemplate}
 * returns a singleton collection containing itself when asked for its {@link #getMarks() marks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkTemplate extends ControlPointTemplate, CommonMarkProperties, HasTags {
}
