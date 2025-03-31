package com.sap.sailing.gwt.ui.shared.courseCreation;

import com.sap.sse.common.Named;

public abstract class ControlPointWithMarkConfigurationDTO implements Named {
    private static final long serialVersionUID = -5408248989419043559L;

    public abstract Iterable<MarkConfigurationDTO> getMarkConfigurations();

    public abstract String getShortName();
}
