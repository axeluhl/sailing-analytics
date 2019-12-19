package com.sap.sailing.domain.coursetemplate;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * A {@link Course} can be created from this template. The template defines {@link MarkRole}s, {@link MarkTemplate}s,
 * {@link ControlPointTemplate}s, and {@link WaypointTemplate}s and assembles the latter into a sequence. The sequence
 * of waypoint templates can optionally have a {@link #getRepeatablePart() repeatable part}. When increasing or reducing
 * the number of laps, more or, respectively, fewer occurrences of this repeatable part will be inserted into the
 * {@link Course}.
 * <p>
 * 
 * The course template can define additional mark templates that are not part of the waypoint template sequence. This
 * can be used, e.g., to define spare marks. Those additional {@link MarkTemplate}s can optionally tell a
 * {@link MarkRole} through the {@link #getDefaultMarkRolesForMarkTemplates()} map as their preferred role for which they
 * define a spare.
 * <p>
 * 
 * All {@link MarkRole}s reachable through the {@link #getWaypointTemplates() waypoint template sequence} must refer to
 * a corresponding {@link MarkTemplate}. This link is represented by the {@link #getDefaultMarkTemplatesForMarkRoles()}
 * method. For all {@link MarkTemplate}s connected to a role in this way, {@link #getDefaultMarkRolesForMarkTemplates()}
 * must contain the reverse link.
 * <p>
 * 
 * The course template has a {@link #getId() globally unique ID} and with this can have a life cycle. Its
 * {@link #getWaypointTemplates() waypoint template sequence} is immutable. If a logical copy is created, the new copy
 * will have a different ID ("Save as...").
 * <p>
 * 
 * The {@link MarkTemplate}s and {@link MarkRole}s also have their globally unique IDs. When creating a variant, the new
 * copy can reference the <em>same</em> set or a subset of the mark templates that the original references. This is
 * helpful in case tracking options or bindings to physical marks are created and remembered. For example, a course
 * template for an "I" (inner loop) course and a course template for an "O" (outer loop) course may share the mark
 * templates for the start line and the "1" top mark, and when tracker bindings are established they can automatically
 * be applied to all marks created from the same {@link MarkTemplate}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseTemplate extends WithOptionalRepeatablePart, NamedWithUUID, HasTags, WithQualifiedObjectIdentifier {
    /**
     * A short name, as typically used in sailing instructions documents, such as "L" for a Windward/Leeward course template
     * with leeward finish. When combined with the number of laps, a well-known short name such as "L2" or "L3" can result.
     */
    String getShortName();
    
    /**
     * The templates for all the marks that shall be made available in the regatta when applying this template. All
     * marks required to construct the waypoint sequence must be produced from this set of mark templates. There may be
     * additional mark templates returned by this method, for constructing marks not immediately required for the
     * waypoint sequence but, e.g., as proposals for spare or alternative marks. For example, templates for alternative
     * marks for the windward mark may be returned to quickly accommodate for wind shifts.
     */
    Iterable<MarkTemplate> getMarkTemplates();
    
    /**
     * The set of {@link MarkRole}s one gets when enumerating all {@link #getWaypointTemplates() waypoint templates},
     * fetching their {@link WaypointTemplate#getControlPointTemplate() control point template} and from it all
     * {@link ControlPointTemplate#getMarkRoles() mark roles}.
     */
    default Iterable<MarkRole> getMarkRoles() {
        final Set<MarkRole> result = new HashSet<>();
        for (final WaypointTemplate wpt : getWaypointTemplates()) {
            for (final MarkRole markRole : wpt.getControlPointTemplate().getMarkRoles()) {
                result.add(markRole);
            }
        }
        return result;
    }
    
    MarkTemplate getMarkTemplateByIdIfContainedInCourseTemplate(UUID markTemplateId);
    
    /**
     * Returns a sequence of {@link WaypointTemplate}s that can be use to construct a course. If this course template
     * defines a repeatable waypoint sub-sequence, the {@code numberOfLaps} parameter is used to decide how many times
     * to repeat this sub-sequence. Typically, the repeatable sub-sequence will be repeated one times fewer than the
     * {@code numberOfLaps}. For example, in a typical windward-leeward "L" course we would have
     * {@code Start/Finish, [1, 4p/4s], 1, Start/Finish}. For an "L1" course with only one lap, we'd like to have
     * {@code Start/Finish, 1, Start/Finish}, so the repeatable sub-sequence, enclosed by the brackets in the example
     * above, will occur zero times. For an "L2" the repeatable sub-sequence will occur once, and so on. However, an
     * implementation is free to choose an interpretation of {@code numberOfLaps} that meets callers' expectations.<p>
     * 
     * Note: this method is mostly intended for testing purposes; a client / UI would normally use a {@link CourseConfiguration}
     * that refers to a {@link CourseTemplate} and then invoke {@link CourseConfiguration#getWaypoints(int)}.
     * 
     * @param numberOfLaps
     *            if the course defines a repeatable part, the number of laps at least needs to be {@code 1} for the
     *            default implementation, and an {@link IllegalArgumentException} shall be thrown in case a value less
     *            than {@code 1} is used if this template specifies a repeatable part. Note again that the number of
     *            repetitions of the repeatable part is usually one less than the number of laps, therefore this
     *            limitation.
     */
    Iterable<WaypointTemplate> getWaypointTemplates(int numberOfLaps);

    Iterable<WaypointTemplate> getWaypointTemplates();

    /**
     * @return the value set contains at least all {@link MarkRole}s reachable through the {@link #getWaypointTemplates()
     *         waypoint template sequence}; the keys for those values are what clients get when calling
     *         {@link #getDefaultMarkTemplateForMarkRole(MarkRole)} for the value. Optionally, additional default role
     *         assignment for "spare" marks, such as the "1" role for alternative windward mark templates may be returned
     *         by this method.
     */
    Map<MarkTemplate, MarkRole> getDefaultMarkRolesForMarkTemplates();
    
    /**
     * Short for {@link #getDefaultMarkRolesForMarkTemplates()}.{@link Map#get(Object) get(markTemplate)}
     */
    MarkRole getOptionalAssociatedRole(MarkTemplate markTemplate);

    /**
     * The key set contains exactly those {@link MarkRole}s one gets when enumerating all roles reachable
     * through the {@link #getWaypointTemplates()} and their {@link WaypointTemplate#getControlPointTemplate()}'s
     * {@link ControlPointTemplate#getMarkRoles() mark roles}. The values tell the {@link MarkTemplate} to use when
     * instantiating a course from this template in place of the key role. All mark templates from the value set
     * are guaranteed to be in {@link #getMarkTemplates()}, but {@link #getMarkTemplates()} may contain more elements,
     * e.g., for spare marks.
     */
    Map<MarkRole, MarkTemplate> getDefaultMarkTemplatesForMarkRoles();
    
    /**
     * Obtains the {@link MarkTemplate} that, when instantiating a course from this template, shall be used
     * to create the {@link Mark} that acts in the role identified by {@code markRole}. Short for
     * {@link #getDefaultMarkTemplatesForMarkRoles()}.{@link Map#get(Object) get(markRole)}.
     */
    MarkTemplate getDefaultMarkTemplateForMarkRole(MarkRole markRole);
    
    MarkRole getMarkRoleByIdIfContainedInCourseTemplate(UUID markRoleId);
    
    URL getOptionalImageURL();

    Integer getDefaultNumberOfLaps();
    
    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID courseTemplateUUID) {
        return new TypeRelativeObjectIdentifier(courseTemplateUUID.toString());
    }
    
    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier(getId()));
    }
    
    @Override
    default HasPermissions getPermissionType() {
        return SecuredDomainType.COURSE_TEMPLATE;
    }

}
