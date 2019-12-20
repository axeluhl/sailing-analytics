package com.sap.sailing.domain.coursetemplate;

/**
 * Describes a request to construct a mark from a {@link MarkConfiguration}. As such, it differs from
 * {@link MarkConfigurationResponse} in that it may provide {@link #getOptionalPositioning() positioning information}
 * that the receiver of this object in expected to implement. In contract, this object does not contain
 * information about the actual positioning situation as the server would see it.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkConfigurationRequest extends MarkConfiguration<MarkConfigurationRequest> {
    boolean isStoreToInventory();
    
    Positioning getOptionalPositioning();
}
