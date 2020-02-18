package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.Util;

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
    
    /**
     * Can be used to provide the name/short name for a {@link MarkRole} to use for this mark when a
     * {@link CourseTemplate} is created from the {@link CourseConfiguration} containing the {@link MarkConfiguration}
     * that is annotated with {@code this} object.<p>
     * 
     * An existing {@link MarkRole} object would be referenced by the {@link CourseConfiguration}'s
     * {@link CourseConfiguration#getAssociatedRoles()} map and therefore wouldn't need any mark role
     * creation request as part of this request annotation.
     */
    MarkRoleCreationRequest getOptionalMarkRoleCreationRequest();
    
    /**
     * Describes a request to create a {@link MarkRole} in the context of a {@link CourseTemplate}. If several
     * {@link MarkConfiguration}s request the creation of {@link MarkRole}s using a {@link MarkRoleCreationRequest} then
     * those requests will be merged based on name and short name equality during {@link CourseTemplate} creation. Both,
     * name and short name, have to be {@link Util#equalsWithNull(Object, Object) equal} in order to cause a merge.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    interface MarkRoleCreationRequest {
        String getMarkRoleName();
        String getMarkRoleShortName();
    }
}
