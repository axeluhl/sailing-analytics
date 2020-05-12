package com.sap.sailing.domain.coursetemplate;

public interface MarkConfigurationVisitor<T, P> {
    T visit(FreestyleMarkConfiguration<P> markConfiguration);
    T visit(MarkPropertiesBasedMarkConfiguration<P> markConfiguration);
    T visit(MarkTemplateBasedMarkConfiguration<P> markConfiguration);
    T visit(RegattaMarkConfiguration<P> markConfiguration);
}
