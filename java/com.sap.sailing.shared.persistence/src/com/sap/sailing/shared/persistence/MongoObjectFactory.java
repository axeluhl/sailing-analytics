package com.sap.sailing.shared.persistence;

import java.util.UUID;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.shared.persistence.device.DeviceIdentifierMongoHandler;
import com.sap.sse.common.TypeBasedServiceFinder;

public interface MongoObjectFactory {

    MongoDatabase getDatabase();

    /** Stores {@link MarkProperties} from the database. */
    void storeMarkProperties(TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder,
            MarkProperties markProperties);

    /** Removes a {@link MarkProperties} object from the database. */
    void removeMarkProperties(UUID markPropertiesId);

    void storeMarkTemplate(MarkTemplate markTemplate);

    void removeMarkTemplate(UUID markTemplateId);

    void storeCourseTemplate(CourseTemplate courseTemplate);

    void removeCourseTemplate(UUID courseTemplateId);
    
    void storeMarkRole(MarkRole markRole);
    
    void removeMarkRole(UUID markRoleId);

}
