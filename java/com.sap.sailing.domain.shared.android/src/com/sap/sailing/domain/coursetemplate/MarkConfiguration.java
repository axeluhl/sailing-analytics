package com.sap.sailing.domain.coursetemplate;

import java.util.Collections;

import com.sap.sailing.domain.base.Mark;

/**
 * Describes how a {@link Mark} in a regatta shall be configured, in terms of its basic attributes as well as regarding
 * its positioning / tracking / location information.
 * <p>
 * 
 * The information may come from one or more sources:
 * <ul>
 * <li>A {@link MarkTemplate} can provide {@link CommonMarkProperties}</li>
 * <li>An existing {@link Mark} from the regatta may be selected; the mark may or may not be used to fill the role
 * defined by the {@link #getOptionalMarkTemplate() mark template}. Regarding the {@link #getEffectiveProperties()
 * effective properties}, the {@link Mark}'s properties take precedence over the {@link MarkTemplate}'s properties.</li>
 * <li>A {@link MarkProperties} object may be selected from the inventory or may have been
 * {@link #getOptionalMarkProperties() identified} as having been used while creating the {@link #getMark() mark} in the
 * regatta, to provide optional base configuration ({@link CommonMarkProperties}) as well as tracking/location/device
 * information. All of those are optional in a {@link MarkProperties} object. Those present will take precedence and
 * will be merged with the properties obtained from the {@link #getOptionalMarkTemplate() mark template} and the
 * properties obtained from an existing {@link #getMark() mark}.</li>
 * </ul>
 * 
 * The combined set of properties merged according to the rules above can be obtained by calling
 * {@link #getEffectiveProperties()}.
 * <p>
 * 
 * The {@link #isStoreToInventory()} method tells whether, when applying this configuration to create or use a
 * {@link Mark}, the {@link #getEffectiveProperties() effective mark properties} shall be recorded as a new
 * {@link MarkProperties} object in the inventory.
 * 
 * @param <P>
 *            type of the annotation used to convey positioning-related information; typical instantiations would, e.g.,
 *            be with {@link MarkConfigurationRequestAnnotation} and {@link MarkConfigurationResponseAnnotation}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkConfiguration<P> extends ControlPointWithMarkConfiguration<P> {
    MarkTemplate getOptionalMarkTemplate();
    
    MarkProperties getOptionalMarkProperties();
    
    CommonMarkProperties getEffectiveProperties();
    
    P getAnnotationInfo();
    
    @Override
    default Iterable<MarkConfiguration<P>> getMarkConfigurations() {
        final Iterable<MarkConfiguration<P>> result = (Iterable<MarkConfiguration<P>>) Collections.singleton(this);
        return result;
    }
}
