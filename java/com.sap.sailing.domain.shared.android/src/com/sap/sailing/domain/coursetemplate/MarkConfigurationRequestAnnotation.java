package com.sap.sailing.domain.coursetemplate;

/**
 * Additional data for a {@link MarkConfiguration} when using it in a request, e.g., to create a course. It may provide
 * {@link #getOptionalPositioning() positioning information} that the receiver of this object in expected to implement.
 * In contrast, this object does not contain information about the actual positioning situation as the server would see
 * it.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkConfigurationRequestAnnotation {
    boolean isStoreToInventory();
    
    Positioning getOptionalPositioning();
}
