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
public interface FreestyleMarkConfiguration<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        extends MarkConfiguration<MarkConfigurationT> {
    CommonMarkProperties getFreestyleProperties();

    /**
     * @return a non-{@code null} object if this mark configuration originated from a {@link MarkProperties} object
     *         where the {@link #getFreestyleProperties() freestyle properties} override one or more of the
     *         {@link MarkProperties}' attributes. If {@link #isStoreToInventory()} is {@code true}, this means
     *         that the {@link MarkProperties} object returned by this method shall be updated accordingly to
     *         reflect the {@link #getFreestyleProperties() freestyle properties} after the update.
     */
    @Override
    MarkProperties getOptionalMarkProperties();
}
