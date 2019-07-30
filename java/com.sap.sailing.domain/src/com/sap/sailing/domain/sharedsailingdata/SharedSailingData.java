package com.sap.sailing.domain.sharedsailingdata;

import java.util.List;
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

public interface SharedSailingData {
    
    Iterable<MarkProperties> getAllMarkProperties();
    
    Iterable<MarkTemplate> getAllMarkTemplates();
    
    Iterable<CourseTemplate> getAllCourseTemplates();

    MarkProperties createMarkProperties(UUID id, CommonMarkProperties properties);
    
    /**
     * This overrides a previously set fixed position or associated tracking device.
     */
    void setFixedPositionForMarkProperties(UUID id, Position position);
    
    /**
     * This overrides a previously set fixed position or associated tracking device.
     */
    void setTrackingDeviceIdentifierForMarkProperties(UUID id, DeviceIdentifier deviceIdentifier);
    
    MarkTemplate createMarkTemplate(UUID id, CommonMarkProperties properties);
    
    CourseTemplate createCourseTemplate(UUID id, Iterable<MarkTemplate> marks, List<WaypointTemplate> waypoints,
            int zeroBasedIndexOfRepeatablePartStart, int zeroBasedIndexOfRepeatablePartEnd);
    
    void recordUsage(MarkTemplate markTemplate, MarkProperties markProperties);
    
    Map<MarkProperties, TimePoint> getUsedMarkProperties(MarkTemplate markTemplate);
    
    void deleteMarkProperties(UUID id);
    
    void deleteMarkTemplate(UUID id);
    
    void deleteCourseTemplate(UUID id);
}
