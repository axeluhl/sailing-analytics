package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.osgi.util.tracker.ServiceTracker;

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
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.Util;
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

    private static final Logger LOG = Logger.getLogger(SharedSailingDataImpl.class.getName());
    private final DomainObjectFactory domainObjectFactory;
    private final MongoObjectFactory mongoObjectFactory;

    private final Map<UUID, MarkProperties> markPropertiesById = new ConcurrentHashMap<>();
    private final Map<UUID, MarkTemplate> markTemplatesById = new ConcurrentHashMap<>();
    private final TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder;
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;

    public SharedSailingDataImpl(final DomainObjectFactory domainObjectFactory,
            final MongoObjectFactory mongoObjectFactory,
            TypeBasedServiceFinder<DeviceIdentifierMongoHandler> deviceIdentifierServiceFinder,
            ServiceTracker<SecurityService, SecurityService> securityServiceTracker) {
        this.domainObjectFactory = domainObjectFactory;
        this.mongoObjectFactory = mongoObjectFactory;
        this.deviceIdentifierServiceFinder = deviceIdentifierServiceFinder;
        this.securityServiceTracker = securityServiceTracker;
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

        // TODO: ensure mark templates are loaded

        // TODO: synchronization
        if (markPropertiesById.isEmpty()) {
            domainObjectFactory.loadAllMarkProperties(v -> markTemplatesById.get(v))
                    .forEach(m -> markPropertiesById.put(m.getId(), m));
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
    public MarkProperties createMarkProperties(CommonMarkProperties properties,
            Iterable<String> tags) {
        final UUID idOfNewMarkProperties = UUID.randomUUID();
        apply(s -> s.internalCreateMarkProperties(idOfNewMarkProperties, properties, tags));
        return getMarkPropertiesById(idOfNewMarkProperties);
    }
    
    @Override
    public Void internalCreateMarkProperties(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags) {
        if (markPropertiesById.containsKey(idOfNewMarkProperties)) {
            LOG.warning(
                    String.format("Found a mark properties with ID %s, overwrite.", idOfNewMarkProperties.toString()));
        }
        final MarkProperties markProperties = new MarkPropertiesBuilder(idOfNewMarkProperties, properties.getName(),
                properties.getShortName(), properties.getColor(), properties.getShape(), properties.getPattern(),
                properties.getType()).withTags(tags).build();

        // TODO: synchronization
        markPropertiesById.put(markProperties.getId(), markProperties);
        mongoObjectFactory.storeMarkProperties(deviceIdentifierServiceFinder, markProperties);
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
        return markPropertiesById.get(id);
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
    public MarkTemplate createMarkTemplate(CommonMarkProperties properties,
            Iterable<String> tags) {
        final UUID idOfNewMarkTemplate = UUID.randomUUID();
        apply(s -> s.internalCreateMarkTemplate(idOfNewMarkTemplate, properties, tags));
        return getMarkTemplateById(idOfNewMarkTemplate);
    }
    
    @Override
    public Void internalCreateMarkTemplate(UUID idOfNewMarkTemplate, CommonMarkProperties properties,
            Iterable<String> tags) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public MarkTemplate getMarkTemplateById(UUID id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseTemplate createCourseTemplate(Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, int zeroBasedIndexOfRepeatablePartStart,
            int zeroBasedIndexOfRepeatablePartEnd, Iterable<String> tags) {
        final UUID idOfNewCourseTemplate = UUID.randomUUID();
        apply(s -> s.internalCreateCourseTemplate(idOfNewCourseTemplate, marks, waypoints,
                zeroBasedIndexOfRepeatablePartStart, zeroBasedIndexOfRepeatablePartEnd, tags));
        return getCourseTemplateById(idOfNewCourseTemplate);
    }
    
    @Override
    public Void internalCreateCourseTemplate(UUID idOfNewCourseTemplate, Iterable<MarkTemplate> marks,
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
        // TODO Auto-generated method stub

    }

    @Override
    public Map<MarkProperties, TimePoint> getUsedMarkProperties(MarkTemplate markTemplate) {
        // TODO Auto-generated method stub
        return null;
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
