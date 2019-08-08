package com.sap.sailing.server.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBuilder;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.Util;

public class SharedSailingDataImpl implements SharedSailingData {
    
    private final DomainObjectFactory domainObjectFactory;
    private final MongoObjectFactory mongoObjectFactory;

    private final Map<UUID, MarkProperties> markPropertiesById = new ConcurrentHashMap<>();
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder;


    public SharedSailingDataImpl(final DomainObjectFactory domainObjectFactory,
            final MongoObjectFactory mongoObjectFactory,
            TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder) {
        this.domainObjectFactory = domainObjectFactory;
        this.mongoObjectFactory = mongoObjectFactory;
        this.deviceIdentifierServiceFinder = deviceIdentifierServiceFinder;
        
    }

    @Override
    public Iterable<MarkProperties> getAllMarkProperties(Iterable<String> tagsToFilterFor) {

        // TODO: synchronization
        if (markPropertiesById.isEmpty()) {
            domainObjectFactory.loadAllMarkProperties().forEach(m -> markPropertiesById.put(m.getId(), m));
        }
        
        return markPropertiesById.values().stream().filter(m -> containsAny(m.getTags(), tagsToFilterFor))
                .collect(Collectors.toList());
    }

    private <T> boolean containsAny(Iterable<T> iterable, Iterable<T> search) {
        for (T t : search) {
            if (Util.contains(iterable, t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<MarkTemplate> getAllMarkTemplates(Iterable<String> tagsToFilterFor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<CourseTemplate> getAllCourseTemplates(Iterable<String> tagsToFilterFor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkProperties createMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags) {
        final MarkProperties markProperties = new MarkPropertiesBuilder(idOfNewMarkProperties, properties.getName(),
                properties.getShortName(), properties.getColor(), properties.getShape(), properties.getPattern(),
                properties.getType()).withTags(tags).build();

        // TODO: synchronization
        markPropertiesById.put(markProperties.getId(), markProperties);
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
        return markProperties;
    }

    @Override
    public void setFixedPositionForMarkProperties(MarkProperties markProperties, Position position) {
        markProperties.setFixedPosition(position);
        markPropertiesById.put(markProperties.getId(), markProperties);
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
    }

    @Override
    public MarkProperties getMarkPropertiesById(UUID id) {
        return markPropertiesById.get(id);
    }

    @Override
    public void setTrackingDeviceIdentifierForMarkProperties(MarkProperties markProperties,
            DeviceIdentifier deviceIdentifier) {
        markProperties.setTrackingDeviceIdentifier(deviceIdentifier);
        markPropertiesById.put(markProperties.getId(), markProperties);
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);

    }

    @Override
    public MarkTemplate createMarkTemplate(UUID idOfNewMarkTemplate, CommonMarkProperties properties,
            Iterable<String> tags) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseTemplate createCourseTemplate(UUID idOfNewCourseTemplate, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, int zeroBasedIndexOfRepeatablePartStart,
            int zeroBasedIndexOfRepeatablePartEnd, Iterable<String> tags) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recordUsage(MarkTemplate markTemplate, MarkProperties markProperties) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<MarkProperties, TimePoint> getUsedMarkProperties(MarkTemplate markTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteMarkProperties(MarkProperties markProperties) {
            // TODO Auto-generated method stub
            
    }

    @Override
    public void deleteCourseTemplate(CourseTemplate courseTemplate) {
        // TODO Auto-generated method stub

    }

}
