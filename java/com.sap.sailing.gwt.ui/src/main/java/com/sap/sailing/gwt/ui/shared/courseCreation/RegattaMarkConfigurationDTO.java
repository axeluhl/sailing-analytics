package com.sap.sailing.gwt.ui.shared.courseCreation;

import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class RegattaMarkConfigurationDTO extends MarkConfigurationDTO {

    private static final long serialVersionUID = -9209811588621240132L;
    private MarkDTO mark;
    private MarkPropertiesDTO optionalMarkProperties;

    @Override
    public String getName() {
        return mark.getName();
    }

    @Override
    public MarkPropertiesDTO getOptionalMarkProperties() {
        return optionalMarkProperties;
    }

    @Override
    public void setOptionalMarkTemplate(MarkTemplateDTO optionalMarkTemplate) {
        super.setOptionalMarkTemplate(optionalMarkTemplate);
    }

    @Override
    public CommonMarkPropertiesDTO getEffectiveProperties() {
        return new CommonMarkPropertiesDTO(mark.getName(), mark.getShortName(), mark.color, mark.shape, mark.pattern, mark.type);
    }

    @Override
    public String getShortName() {
        return mark.getShortName();
    }

    public MarkDTO getMark() {
        return mark;
    }

    public void setMark(MarkDTO mark) {
        this.mark = mark;
    }
}
