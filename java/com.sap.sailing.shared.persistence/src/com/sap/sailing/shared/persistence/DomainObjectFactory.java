package com.sap.sailing.shared.persistence;

import java.util.UUID;
import java.util.function.Function;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public interface DomainObjectFactory {

    MongoDatabase getDatabase();

    Iterable<MarkProperties> loadAllMarkProperties(Function<UUID, MarkTemplate> markTemplateResolver,
            Function<UUID, MarkRole> markRoleResolver);

    Iterable<MarkTemplate> loadAllMarkTemplates();

    Iterable<MarkRole> loadAllMarkRoles();

    Iterable<CourseTemplate> loadAllCourseTemplates(Function<UUID, MarkTemplate> markTemplateResolver,
            Function<UUID, MarkRole> markRoleResolver);
}
