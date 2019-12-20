package com.sap.sailing.domain.coursetemplate;

/**
 * The {@link #getOptionalMarkTemplate()} method will always return a non-{@code null} {@link MarkTemplate}
 * which is the only source of the properties returned by {@link #getEffectiveProperties()}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkTemplateBasedMarkConfiguration<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        extends MarkConfiguration<MarkConfigurationT> {
}
