package com.sap.sailing.domain.coursetemplate;

/**
 * Describes a configuration with an optional {@link #getMarkTemplate() mark template} that delivers
 * explicit {@link CommonMarkPropertiesWithOptionalPositioning}. The object can be marked for
 * adding to the inventory when applied. The {@link #getFreestyleProperties() freestyle properties}
 * take precedence.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface FreestyleMarkConfiguration extends MarkConfiguration {
    @Override
    MarkTemplate getMarkTemplate();

    CommonMarkPropertiesWithOptionalPositioning getFreestyleProperties();

    @Override
    CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties();

    @Override
    boolean isStoreToInventory();

}
