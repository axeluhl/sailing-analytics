package com.sap.sailing.domain.coursetemplate;

/**
 * Configures a mark based on a {@link MarkProperties} object returned from {@link #getMarkProperties()}. Optionally, a
 * non-{@code null} {@link MarkTemplate} may be returned from {@link #getOptionalMarkTemplate()} which then means that the mark
 * to create and configure assumes the "role" defined by the mark template. The {@link MarkProperties} attributes,
 * if present, take precedence over their respective {@link MarkTemplate} counterparts.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkPropertiesBasedMarkConfiguration extends MarkConfiguration {
    MarkProperties getMarkProperties();

    /**
     * No need to store a {@link MarkProperties} object to the inventory; we are using an unmodified
     * {@link MarkProperties} object returned by {@link #getMarkProperties()} already.
     */
    @Override
    default boolean isStoreToInventory() {
        return false;
    }
}
