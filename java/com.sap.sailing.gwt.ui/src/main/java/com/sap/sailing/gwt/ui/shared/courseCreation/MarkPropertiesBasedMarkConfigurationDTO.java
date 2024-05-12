package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.util.Arrays;

public class MarkPropertiesBasedMarkConfigurationDTO extends MarkConfigurationDTO {

    private static final long serialVersionUID = 2204116830344315567L;
    private MarkPropertiesDTO markProperties;
    
    public void setMarkProperties(MarkPropertiesDTO markProperties) {
        this.markProperties = markProperties;
    }
    
    @Override
    public MarkPropertiesDTO getOptionalMarkProperties() {
        return markProperties;
    }

    @Override
    public CommonMarkPropertiesDTO getEffectiveProperties() {
        return markProperties.getCommonMarkProperties();
    }

    @Override
    public Iterable<MarkConfigurationDTO> getMarkConfigurations() {
        return Arrays.asList(this);
    }

    @Override
    public String getName() {
        return markProperties.getName();
    }

    @Override
    public String getShortName() {
        return markProperties.getCommonMarkProperties().getShortName();
    }

}
