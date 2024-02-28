package com.sap.sailing.gwt.ui.shared.courseCreation;

public class MarkTemplateBasedMarkConfigurationDTO extends MarkConfigurationDTO {

    private static final long serialVersionUID = 4766727754528156938L;

    @Override
    public String getName() {
        return getOptionalMarkTemplate().getName();
    }

    @Override
    public MarkPropertiesDTO getOptionalMarkProperties() {
        return null;
    }

    @Override
    public CommonMarkPropertiesDTO getEffectiveProperties() {
        return getOptionalMarkTemplate().getCommonMarkProperties();
    }

    @Override
    public String getShortName() {
        return getOptionalMarkTemplate().getCommonMarkProperties().getShortName();
    }
}
