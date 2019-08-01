package com.sap.sailing.domain.coursetemplate;

/**
 * Configures a mark based on a {@link MarkProperties} object returned from {@link #getMarkProperties()}. Optionally, a
 * non-{@code null} {@link MarkTemplate} may be returned from {@link #getMarkTemplate()} which then means that the mark
 * to create and configure assumes the "role" defined by the mark template. The {@link MarkProperties} attributes,
 * if present, take precedence over their respective {@link MarkTemplate} counterparts.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkPropertiesMarkTemplateMapping extends MarkConfiguration {
    @Override
    MarkTemplate getMarkTemplate();

    @Override
    MarkProperties getMarkProperties();

    @Override
    CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties();
}
