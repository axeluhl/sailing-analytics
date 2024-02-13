package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.util.Arrays;

public abstract class MarkConfigurationDTO extends ControlPointWithMarkConfigurationDTO {
    private static final long serialVersionUID = -6944495722826431655L;
    private MarkTemplateDTO optionalMarkTemplate;

    public MarkTemplateDTO getOptionalMarkTemplate() {
        return optionalMarkTemplate;
    }

    public void setOptionalMarkTemplate(MarkTemplateDTO optionalMarkTemplate) {
        this.optionalMarkTemplate = optionalMarkTemplate;
    }

    public abstract MarkPropertiesDTO getOptionalMarkProperties();

    public abstract CommonMarkPropertiesDTO getEffectiveProperties();

    @Override
    public Iterable<MarkConfigurationDTO> getMarkConfigurations() {
        return Arrays.asList(this);
    }
}
