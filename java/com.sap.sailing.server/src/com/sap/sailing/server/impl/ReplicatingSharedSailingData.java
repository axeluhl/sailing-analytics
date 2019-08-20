package com.sap.sailing.server.impl;

import java.net.URL;
import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

public interface ReplicatingSharedSailingData extends SharedSailingData,
        ReplicableWithObjectInputStream<ReplicatingSharedSailingData, OperationWithResult<ReplicatingSharedSailingData, ?>> {

    Void internalCreateMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags);
    
    Void internalCreateMarkTemplate(UUID idOfNewMarkTemplate, CommonMarkProperties properties);
    
    Void internalCreateCourseTemplate(UUID idOfNewCourseTemplate, String courseTemplateName, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, RepeatablePart optionalRepeatablePart,
            Iterable<String> tags, URL optionalImageURL);
    
    Void internalSetTrackingDeviceIdentifierForMarkProperties(UUID markPropertiesUUID, DeviceIdentifier deviceIdentifier);
    
    Void internalSetFixedPositionForMarkProperties(UUID markPropertiesUUID, Position position);

    Void internalDeleteMarkProperties(UUID markPropertiesUUID);

    Void internalDeleteCourseTemplate(UUID courseTemplateUUID);

    Void internalRecordUsage(UUID markTemplateId, UUID markPropertiesId);

}
