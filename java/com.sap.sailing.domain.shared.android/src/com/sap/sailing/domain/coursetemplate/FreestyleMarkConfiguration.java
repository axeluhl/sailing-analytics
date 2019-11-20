package com.sap.sailing.domain.coursetemplate;

/**
 * Describes a configuration with an optional {@link #getOptionalMarkTemplate() mark template} that delivers
 * explicit {@link CommonMarkPropertiesWithOptionalPositioning}. The object can be marked for
 * adding to the inventory when applied. The {@link #getFreestyleProperties() freestyle properties}
 * take precedence.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface FreestyleMarkConfiguration extends MarkConfiguration {
    CommonMarkProperties getFreestyleProperties();

    
    /**
     * @return a non-{@code null} object if this mark configuration originated from a {@link MarkProperties}
     * object where the {@link #getFreestyleProperties() freestyle properties} override one or more of the
     * {@link MarkProperties}' attributes.
     */
    MarkProperties getOptionalMarkProperties();
}
