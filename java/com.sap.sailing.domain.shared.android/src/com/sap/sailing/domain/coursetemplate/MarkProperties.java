package com.sap.sailing.domain.coursetemplate;

import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Color;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * Stores properties that can be applied to a mark in the context of an event or a regatta, including the mark's own
 * attributes as well as tracking-related information such as a reference to the tracking device used to track the mark,
 * or a fixed mark position, such as for a land mark, an official lateral or cardinal buoy, a regatta mark in a fixed
 * position or a lighthouse.
 * <p>
 * 
 * Such a properties object can be linked to zero or more {@link MarkTemplate}s. When the user creates a course from a
 * {@link CourseTemplate}, linked {@link MarkProperties} can be offered to configure the {@link Mark} created from the
 * {@link CourseTemplate}'s {@link MarkTemplate}s.
 * <p>
 * 
 * {@link MarkProperties} objects can be tagged, and the tags may be used to search and filter a library of such
 * {@link MarkProperties}. The tags can, e.g., express a venue name or a course area name or the name of a set of
 * tracking devices typically used to track a course. A user may have more than one {@link MarkProperties} object
 * available that is linked to a particular {@link MarkTemplate}. One of those could be selected depending on
 * {@link #getPreviousUsage() when it was used last} for the {@link MarkTemplate}, or the user may be presented with the
 * set of {@link MarkProperties} available and can select, search and filter.<p>
 * 
 * These properties objects are entities and can be updated. For example, if the device used to track the mark
 * shall be replaced permanently, also for future uses, the device ID could be updated.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkProperties extends CommonMarkPropertiesWithOptionalPositioning, NamedWithUUID, HasTags, WithQualifiedObjectIdentifier {
    void setColor(Color color);

    void setShape(String shape);

    void setPattern(String pattern);

    void setType(MarkType type);

    void setShortName(String shortName);

    /**
     * Updates this properties object such that the next call to {@link #getTrackingDeviceIdentifier()} returns the
     * {@code deviceIdentifier} provided to this call. The {@code deviceIdentifier} may be {@code null}, meaning that no
     * default tracking device mapping is desired for marks configured with these properties. It is an error, and an
     * {@link IllegalStateException} will be thrown, if a non-{@code null} {@link #getFixedPosition() fixed position} is
     * currently defined by these properties because a mark cannot have a fixed position and be tracked by a device at
     * the same time.
     */
    void setTrackingDeviceIdentifier(DeviceIdentifier deviceIdentifier);
    
    /**
     * Provides a fixed position to be set by means of a "ping" when these properties are applied to a mark. The
     * {@code fixedPosition} parameter can be {@code null}, meaning that no fixed position is known (anymore). It is an
     * error, and an {@link IllegalStateException} will be thrown, if a non-{@code null}
     * {@link #getTrackingDeviceIdentifier() tracking device identifier} is currently defined for these mark properties
     * because a mark cannot have a fixed position and be tracked by a device at the same time.
     */
    void setFixedPosition(Position fixedPosition);

    Map<MarkTemplate, TimePoint> getLastUsedTemplate();

    Map<MarkRole, TimePoint> getLastUsedRole();
    
    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID markPropertiesUUID) {
        return new TypeRelativeObjectIdentifier(markPropertiesUUID.toString());
    }
    
    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier(getId()));
    }
    
    @Override
    default HasPermissions getPermissionType() {
        return SecuredDomainType.MARK_PROPERTIES;
    }
}
