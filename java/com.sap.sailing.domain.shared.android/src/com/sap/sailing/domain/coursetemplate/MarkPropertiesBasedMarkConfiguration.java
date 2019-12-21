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
public interface MarkPropertiesBasedMarkConfiguration<P> extends MarkConfiguration<P> {
    MarkProperties getMarkProperties();
    
    /**
     * The "optional" {@link MarkProperties} for this type of mark configuration are the {@link #getMarkProperties()
     * MarkProperties} referenced by this object and hence never {@code null}.
     */
    @Override
    default MarkProperties getOptionalMarkProperties() {
        return getMarkProperties();
    }
}
