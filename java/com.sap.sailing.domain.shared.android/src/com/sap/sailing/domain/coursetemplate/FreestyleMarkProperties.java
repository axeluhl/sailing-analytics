package com.sap.sailing.domain.coursetemplate;

/**
 * While sharing most of {@link CommonMarkProperties}, {@link MarkProperties} do have the distinct
 * {@link FreestyleMarkConfiguration} property tags.
 * 
 */
public interface FreestyleMarkProperties extends CommonMarkProperties, HasTags {
    void setTags(Iterable<String> tags);
}
