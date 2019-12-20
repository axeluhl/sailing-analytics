package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Mark;

/**
 * Configures a mark solely by referencing an already existing mark. No additional configuration beyond what already
 * exists in the regatta can be specified. The {@link #getEffectiveProperties() effective properties} are simply all
 * aspects of the {@link #getMark() mark} in its {@link CommonMarkProperties} role, without any additional
 * location/position/tracking data.
 * <p>
 * 
 * An optional non-{@code null} {@link MarkTemplate} may be returned from {@link #getOptionalMarkTemplate()} which then
 * leads to recording a usage relation between the regatta {@link Mark} and said {@link MarkTemplate}.
 * {@link #getOptionalMarkProperties()} can reference a {@link MarkProperties} object if the {@link #getMark mark} was
 * originally created from that {@link MarkProperties} object.
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
public interface RegattaMarkConfiguration<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        extends MarkConfiguration<MarkConfigurationT> {
    Mark getMark();
}
