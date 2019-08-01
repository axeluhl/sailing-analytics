package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Mark;

/**
 * Configures a mark solely by referencing an already existing mark. No additional configuration beyond what already
 * exists in the regatta can be specified. The {@link #getEffectiveProperties() effective properties} are simply all
 * aspects of the {@link #getMark() mark} in its {@link CommonMarkProperties} role, without any additional
 * location/position/tracking data.
 * <p>
 * 
 * An optional non-{@code null} {@link MarkTemplate} may be returned from {@link #getMarkTemplate()} which then leads to
 * recording a usage relation between the regatta {@link Mark} and said {@link MarkTemplate}.
 * <p>
 * 
 * The {@link #getEffectiveProperties() effective properties} will contain
 * {@link CommonMarkPropertiesWithOptionalPositioning#getFixedPosition() fixed position data} or a
 * {@link CommonMarkPropertiesWithOptionalPositioning#getTrackingDeviceIdentifier() tracking device ID} in case the mark
 * has such information on the server side.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RegattaMarkConfiguration extends MarkConfiguration {
    @Override
    MarkTemplate getMarkTemplate();

    @Override
    Mark getMark();

    @Override
    CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties();

    @Override
    boolean isStoreToInventory();

}
