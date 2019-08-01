package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Mark;

/**
 * Configures a mark solely by referencing an already existing mark. No additional configuration beyond what already
 * exists in the regatta can be specified. The {@link #getEffectiveProperties() effective properties} are simply
 * all aspects of the {@link #getMark() mark} in its {@link CommonMarkProperties} role, without any additional
 * location/position/tracking data.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ExistingMark extends MarkConfiguration {
    Mark getMark();
    
    @Override
    CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties();

    @Override
    boolean isStoreToInventory();
}
