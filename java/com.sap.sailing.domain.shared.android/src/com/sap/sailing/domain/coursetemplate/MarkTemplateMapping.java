package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Mark;

public interface MarkTemplateMapping extends ControlPointWithMarkTemplateMapping {
    
    MarkTemplate getMarkTemplate();

    Mark getMark();

    MarkProperties getMarkProperties();

    CommonMarkProperties getEffectiveProperties();
    
    boolean isStoreToInventory();
}
