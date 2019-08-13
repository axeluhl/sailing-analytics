package com.sap.sailing.server.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicableWithObjectInputStream;

public interface ReplicatingSharedSailingData extends SharedSailingData,
        ReplicableWithObjectInputStream<ReplicatingSharedSailingData, OperationWithResult<ReplicatingSharedSailingData, ?>> {

    Void internalCreateMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags);
    
    Void internalCreateMarkTemplate(UUID idOfNewMarkTemplate, CommonMarkProperties properties,
            Iterable<String> tags);
    
    Void internalCreateCourseTemplate(UUID idOfNewCourseTemplate, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, int zeroBasedIndexOfRepeatablePartStart,
            int zeroBasedIndexOfRepeatablePartEnd, Iterable<String> tags);
    
    Void internalSetTrackingDeviceIdentifierForMarkProperties(UUID markPropertiesUUID, DeviceIdentifier deviceIdentifier);
    
    Void internalSetFixedPositionForMarkProperties(UUID markPropertiesUUID, Position position);
}
