package com.sap.sailing.domain.sharedsailingdata;

import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * TODO document what this service encapuslated, such as persistence, replication, ...; mention reason for providing UUID in factory methods, regarding security aspects
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SharedSailingData {
    
    Iterable<MarkProperties> getAllMarkProperties(Iterable<String> tagsToFilterFor);
    
    Iterable<MarkTemplate> getAllMarkTemplates(Iterable<String> tagsToFilterFor);
    
    Iterable<CourseTemplate> getAllCourseTemplates(Iterable<String> tagsToFilterFor);

    MarkProperties createMarkProperties(CommonMarkProperties properties, Iterable<String> tags);
    
    /**
     * This overrides a previously set fixed position or associated tracking device.
     */
    void setFixedPositionForMarkProperties(MarkProperties markProperties, Position position);
    
    MarkProperties getMarkPropertiesById(UUID id);
    
    /**
     * This overrides a previously set fixed position or associated tracking device.
     */
    void setTrackingDeviceIdentifierForMarkProperties(MarkProperties markProperties, DeviceIdentifier deviceIdentifier);
    
    MarkTemplate createMarkTemplate(CommonMarkProperties properties, Iterable<String> tags);
    
    MarkTemplate getMarkTemplateById(UUID id);
    
    /**
     * @param waypoints the waypoints in their defined order (iteration order equals order of waypoints in course)
     */
    CourseTemplate createCourseTemplate(String courseTemplateName, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            int zeroBasedIndexOfRepeatablePartStart, int zeroBasedIndexOfRepeatablePartEnd, Iterable<String> tags);
    
    CourseTemplate getCourseTemplateById(UUID id);
    
    /**
     * Records the fact that the {@code markProperties} were used to configure a mark that takes the role defined by the
     * {@code markTemplate}. Keeps the {@link MillisecondsTimePoint#now() current time} of this call which will be
     * returned for {@code markTemplate} when invoking {@link #getUsedMarkProperties(MarkTemplate)}.
     */
    void recordUsage(MarkTemplate markTemplate, MarkProperties markProperties);
    
    /**
     * Returns the time points when {@link MarkProperties} objects were {@link #recordUsage(MarkTemplate, MarkProperties) last used}
     * for the {@link MarkTemplate} passed in the {@code markTemplate} parameter.
     */
    Map<MarkProperties, TimePoint> getUsedMarkProperties(MarkTemplate markTemplate);
    
    void deleteMarkProperties(MarkProperties markProperties);
    
    void deleteCourseTemplate(CourseTemplate courseTemplate);
}
