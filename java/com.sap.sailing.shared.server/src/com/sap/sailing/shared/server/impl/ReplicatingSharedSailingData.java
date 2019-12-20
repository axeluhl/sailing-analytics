package com.sap.sailing.shared.server.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

public interface ReplicatingSharedSailingData extends SharedSailingData,
        ReplicableWithObjectInputStream<ReplicatingSharedSailingData, OperationWithResult<ReplicatingSharedSailingData, ?>> {
    
    Void internalCreateMarkRole(UUID idOfNewMarkRole, String name);

    Void internalCreateMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags);
    
    Void internalUpdateMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties, Iterable<String> tags);

    Void internalCreateMarkTemplate(UUID idOfNewMarkTemplate, CommonMarkProperties properties);
    
    Void internalCreateCourseTemplate(UUID idOfNewCourseTemplate, String courseTemplateName,
            Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> associatedRoles, RepeatablePart optionalRepeatablePart, Iterable<String> tags,
            URL optionalImageURL, Integer defaultNumberOfLaps);
    
    Void internalSetPositioningInformationForMarkProperties(UUID markPropertiesUUID, Positioning positioningInformation);
    
    Void internalDeleteMarkProperties(UUID markPropertiesUUID);

    Void internalDeleteCourseTemplate(UUID courseTemplateUUID);

    Void internalRecordUsage(UUID markTemplateId, UUID markPropertiesId);
    
    Void internalRecordUsage(UUID markPropertiesId, MarkRole roleName);

    Void internalUpdateCourseTemplate(UUID uuid, String name, URL optionalImageURL, ArrayList<String> tags);

}
