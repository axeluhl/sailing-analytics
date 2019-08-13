package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBuilder;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.OperationsToMasterSendingQueue;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.security.SecurityService;
import com.sap.sse.util.ClearStateTestSupport;

public class SharedSailingDataImpl implements ReplicatingSharedSailingData, ClearStateTestSupport {

    private final DomainObjectFactory domainObjectFactory;
    private final MongoObjectFactory mongoObjectFactory;

    private final Map<UUID, MarkProperties> markPropertiesById = new ConcurrentHashMap<>();
    private final Map<UUID, MarkTemplate> markTemplatesById = new ConcurrentHashMap<>();
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder;
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;

    public SharedSailingDataImpl(final DomainObjectFactory domainObjectFactory,
            final MongoObjectFactory mongoObjectFactory,
            TypeBasedServiceFinderFactory serviceFinderFactory,
            ServiceTracker<SecurityService, SecurityService> securityServiceTracker) {
        this.domainObjectFactory = domainObjectFactory;
        this.mongoObjectFactory = mongoObjectFactory;
        this.deviceIdentifierServiceFinder = serviceFinderFactory.createServiceFinder(DeviceIdentifierMongoHandler.class);
        this.securityServiceTracker = securityServiceTracker;

        load();
    }
    
    private void load() {
        // load mark templates before mark properties
        domainObjectFactory.loadAllMarkTemplates().forEach(m -> markTemplatesById.put(m.getId(), m));

        domainObjectFactory.loadAllMarkProperties(v -> markTemplatesById.get(v))
                .forEach(m -> markPropertiesById.put(m.getId(), m));
    }

    @Override
    public void clearState() throws Exception {
        removeAll();
    }

    private void removeAll() {
        markPropertiesById.clear();
    }
    
    public SecurityService getSecurityService() {
        return securityServiceTracker.getService();
    }

    @Override
    public Iterable<MarkProperties> getAllMarkProperties(Iterable<String> tagsToFilterFor) {
        // TODO: synchronization
        return markPropertiesById.values().stream().filter(m -> containsAny(m.getTags(), tagsToFilterFor))
                .filter(getSecurityService()::hasCurrentUserReadPermission)
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
    public Iterable<MarkTemplate> getAllMarkTemplates() {
        return markTemplatesById.values().stream().filter(getSecurityService()::hasCurrentUserReadPermission)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<CourseTemplate> getAllCourseTemplates(Iterable<String> tagsToFilterFor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkProperties createMarkProperties(CommonMarkProperties properties, Iterable<String> tags) {
        final UUID idOfNewMarkProperties = UUID.randomUUID();
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.MARK_PROPERTIES,
                MarkProperties.getTypeRelativeObjectIdentifier(idOfNewMarkProperties),
                idOfNewMarkProperties + "/" + properties.getName(), () -> {
                    apply(s -> s.internalCreateMarkProperties(idOfNewMarkProperties, properties, tags));
                    return getMarkPropertiesById(idOfNewMarkProperties);
                });
    }
    
    @Override
    public Void internalCreateMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags) {
        final MarkProperties markProperties = new MarkPropertiesBuilder(idOfNewMarkProperties, properties.getName(),
                properties.getShortName(), properties.getColor(), properties.getShape(), properties.getPattern(),
                properties.getType()).withTags(tags).build();

        // TODO: synchronization
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
        markPropertiesById.put(markProperties.getId(), markProperties);
        return null;
    }

    @Override
    public void setFixedPositionForMarkProperties(final MarkProperties markProperties, final Position position) {
        final UUID markPropertiesUUID = markProperties.getId();
        apply(s -> s.internalSetFixedPositionForMarkProperties(markPropertiesUUID, position));
    }
    
    @Override
    public Void internalSetFixedPositionForMarkProperties(UUID markPropertiesId, Position position) {
        final MarkProperties markProperties = markPropertiesById.get(markPropertiesId);
        markProperties.setFixedPosition(position);
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
        return null;
    }

    @Override
    public MarkProperties getMarkPropertiesById(UUID id) {
        final MarkProperties markProperties = markPropertiesById.get(id);
        if (markProperties != null) {
            getSecurityService().checkCurrentUserReadPermission(markProperties);
        }
        return markProperties;
    }

    @Override
    public void setTrackingDeviceIdentifierForMarkProperties(final MarkProperties markProperties,
            final DeviceIdentifier deviceIdentifier) {
        final UUID markPropertiesUUID = markProperties.getId();
        apply(s -> s.internalSetTrackingDeviceIdentifierForMarkProperties(markPropertiesUUID, deviceIdentifier));
    }
    
    @Override
    public Void internalSetTrackingDeviceIdentifierForMarkProperties(UUID markPropertiesId,
            DeviceIdentifier deviceIdentifier) {
        final MarkProperties markProperties = markPropertiesById.get(markPropertiesId);
        markProperties.setTrackingDeviceIdentifier(deviceIdentifier);
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
        return null;
    }

    @Override
    public MarkTemplate createMarkTemplate(CommonMarkProperties properties) {
        final UUID idOfNewMarkTemplate = UUID.randomUUID();
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.MARK_TEMPLATE, MarkTemplate.getTypeRelativeObjectIdentifier(idOfNewMarkTemplate),
                idOfNewMarkTemplate + "/" + properties.getName(), () -> {
                    apply(s -> s.internalCreateMarkTemplate(idOfNewMarkTemplate, properties));
                    return getMarkTemplateById(idOfNewMarkTemplate);
                });
    }
    
    @Override
    public Void internalCreateMarkTemplate(UUID idOfNewMarkTemplate, CommonMarkProperties properties) {
        final MarkTemplate markTemplate = new MarkTemplateImpl(idOfNewMarkTemplate, properties);
        mongoObjectFactory.storeMarkTemplate(markTemplate);
        markTemplatesById.put(markTemplate.getId(), markTemplate);
        return null;
    }
    
    @Override
    public MarkTemplate getMarkTemplateById(UUID id) {
        final MarkTemplate markTemplate = markTemplatesById.get(id);
        if (markTemplate != null) {
            getSecurityService().checkCurrentUserReadPermission(markTemplate);
        }
        return markTemplate;
    }

    @Override
    public CourseTemplate createCourseTemplate(String courseTemplateName, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, int zeroBasedIndexOfRepeatablePartStart,
            int zeroBasedIndexOfRepeatablePartEnd, Iterable<String> tags) {
        final UUID idOfNewCourseTemplate = UUID.randomUUID();
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.COURSE_TEMPLATE,
                CourseTemplate.getTypeRelativeObjectIdentifier(idOfNewCourseTemplate),
                idOfNewCourseTemplate + "/" + courseTemplateName, () -> {
                    apply(s -> s.internalCreateCourseTemplate(idOfNewCourseTemplate, courseTemplateName, marks,
                            waypoints, zeroBasedIndexOfRepeatablePartStart, zeroBasedIndexOfRepeatablePartEnd, tags));
                    return getCourseTemplateById(idOfNewCourseTemplate);
                });
    }
    
    @Override
    public Void internalCreateCourseTemplate(UUID idOfNewCourseTemplate, String courseTemplateName, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, int zeroBasedIndexOfRepeatablePartStart,
            int zeroBasedIndexOfRepeatablePartEnd, Iterable<String> tags) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public CourseTemplate getCourseTemplateById(UUID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recordUsage(MarkTemplate markTemplate, MarkProperties markProperties) {
        markProperties.getLastUsedTemplate().put(markTemplate, new MillisecondsTimePoint(System.currentTimeMillis()));
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
    }

    @Override
    public Map<MarkProperties, TimePoint> getUsedMarkProperties(MarkTemplate markTemplate) {
        final Map<MarkProperties, TimePoint> recordedUsage = new HashMap<>();
        for (final MarkProperties mp : markPropertiesById.values()) {
            if (mp.getLastUsedTemplate().containsKey(markTemplate)) {
                recordedUsage.put(mp, mp.getLastUsedTemplate().get(markTemplate));
            }
        }
        return recordedUsage;
    }

    @Override
    public void deleteMarkProperties(MarkProperties markProperties) {
        // TODO: synchronization
        if (this.markPropertiesById.remove(markProperties.getId()) != null) {
            mongoObjectFactory.removeMarkProperties(markProperties.getId());
        } else {
            throw new NullPointerException(
                    String.format("Did not find a mark properties with ID %s", markProperties.getId()));
        }

    }

    @Override
    public void deleteCourseTemplate(CourseTemplate courseTemplate) {
        // TODO Auto-generated method stub

    }
    
    // Replication related methods and fields
    private final ConcurrentHashMap<OperationExecutionListener<ReplicatingSharedSailingData>, OperationExecutionListener<ReplicatingSharedSailingData>> operationExecutionListeners = new ConcurrentHashMap<>();
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoad = ThreadLocal.withInitial(() -> false);
    private ThreadLocal<Boolean> currentlyApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);
    private final Set<OperationWithResultWithIdWrapper<ReplicatingSharedSailingData, ?>> operationsSentToMasterForReplication = new HashSet<>();
    private ReplicationMasterDescriptor master;
    
    /**
     * This field is expected to be set by the {@link ReplicationService} once it has "adopted" this replicable.
     * The {@link ReplicationService} "injects" this service so it can be used here as a delegate for the
     * {@link OperationsToMasterSendingQueue#scheduleForSending(OperationWithResult, OperationsToMasterSender)}
     * method.
     */
    private OperationsToMasterSendingQueue unsentOperationsToMasterSender;

    

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return master;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.master = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.master = null;
    }

    @Override
    public Iterable<OperationExecutionListener<ReplicatingSharedSailingData>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public void addOperationExecutionListener(
            OperationExecutionListener<ReplicatingSharedSailingData> listener) {
        this.operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(
            OperationExecutionListener<ReplicatingSharedSailingData> listener) {
        this.operationExecutionListeners.remove(listener);
    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoad() {
        return currentlyFillingFromInitialLoad.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoad(boolean currentlyFillingFromInitialLoad) {
        this.currentlyFillingFromInitialLoad.set(currentlyFillingFromInitialLoad);
    }

    @Override
    public boolean isCurrentlyApplyingOperationReceivedFromMaster() {
        return currentlyApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyApplyingOperationReceivedFromMaster(boolean currentlyApplyingOperationReceivedFromMaster) {
        this.currentlyApplyingOperationReceivedFromMaster.set(currentlyApplyingOperationReceivedFromMaster);
    }

    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<ReplicatingSharedSailingData, ?> operationWithResultWithIdWrapper) {
        this.operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }

    @Override
    public boolean hasSentOperationToMaster(
            OperationWithResult<ReplicatingSharedSailingData, ?> operation) {
        return operationsSentToMasterForReplication.remove(operation);
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStream(is);
    }

    @Override
    public synchronized void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
    }

    @Override
    public synchronized void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        removeAll();
    }

    @Override
    public void setUnsentOperationToMasterSender(OperationsToMasterSendingQueue service) {
        this.unsentOperationsToMasterSender = service;
    }

    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void scheduleForSending(
            OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {
        if (unsentOperationsToMasterSender != null) {
            unsentOperationsToMasterSender.scheduleForSending(operationWithResult, sender);
        }
    }
}
