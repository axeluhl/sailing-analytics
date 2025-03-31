package com.sap.sailing.gwt.ui.shared.courseCreation;

public class FreestyleMarkConfigurationDTO extends MarkConfigurationDTO {
    private static final long serialVersionUID = -752631017462284881L;
    private CommonMarkPropertiesDTO freestyleProperties;
    private MarkPropertiesDTO optionalMarkProperties;

    @Override
    public CommonMarkPropertiesDTO getEffectiveProperties() {
        return freestyleProperties;
    }

    @Override
    public String getName() {
        return freestyleProperties.getName();
    }

    @Override
    public String getShortName() {
        return freestyleProperties.getShortName();
    }

    public MarkPropertiesDTO getOptionalMarkProperties() {
        return optionalMarkProperties;
    }

    public void setOptionalMarkProperties(MarkPropertiesDTO optionalMarkProperties) {
        this.optionalMarkProperties = optionalMarkProperties;
    }
}
