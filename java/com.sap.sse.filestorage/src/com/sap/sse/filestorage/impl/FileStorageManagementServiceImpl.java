package com.sap.sse.filestorage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.FileStorageServiceProperty;
import com.sap.sse.filestorage.FileStorageServicePropertyStore;
import com.sap.sse.filestorage.FileStorageServiceResolver;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.UnsentOperationsToMasterSender;

/**
 * Implements {@link ServiceTrackerCustomizer} so that all {@link FileStorageServices} announced in the registry can
 * receive their stored properties.
 * 
 * @author Fredrik Teschke
 *
 */
public class FileStorageManagementServiceImpl implements ReplicableFileStorageManagementService,
        ServiceAddedListener<FileStorageService> {
    private final Logger logger = Logger.getLogger(FileStorageManagementServiceImpl.class.getName());

    private FileStorageService active;
    private final TypeBasedServiceFinder<FileStorageService> serviceFinder;
    
    private final Map<OperationExecutionListener<ReplicableFileStorageManagementService>, OperationExecutionListener<ReplicableFileStorageManagementService>> operationExecutionListeners = new ConcurrentHashMap<>();
    private final Set<OperationWithResultWithIdWrapper<?, ?>> operationsSentToMasterForReplication = new HashSet<>();
    private ReplicationMasterDescriptor replicationMasterDescriptor;
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoad = ThreadLocal.withInitial(() -> false);
    
    private ThreadLocal<Boolean> currentlyApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);

    /**
     * Is set to an {@link EmptyFileStorageServicePropertyStoreImpl} on replicas.
     */
    private FileStorageServicePropertyStore propertyStore;
    private final FileStorageServiceResolver serviceResolver;

    /**
     * This field is expected to be set by the {@link ReplicationService} once it has "adopted" this replicable.
     * The {@link ReplicationService} "injects" this service so it can be used here as a delegate for the
     * {@link UnsentOperationsToMasterSender#retrySendingLater(OperationWithResult, OperationsToMasterSender)}
     * method.
     */
    private UnsentOperationsToMasterSender unsentOperationForMasterQueue;

    public FileStorageManagementServiceImpl(TypeBasedServiceFinder<FileStorageService> serviceFinder,
            FileStorageServicePropertyStore propertyStore) {
        this.serviceFinder = serviceFinder;
        this.propertyStore = propertyStore;
        serviceResolver = new FileStorageServiceResolverAgainstOsgiRegistryImpl(serviceFinder);
        active = getFileStorageService(propertyStore.readActiveServiceName());
    }

    @Override
    public FileStorageService getActiveFileStorageService() {
        if (active == null) {
            throw new NoCorrespondingServiceRegisteredException();
        }
        return active;
    }

    @Override
    public void setActiveFileStorageService(FileStorageService service) {
        apply(s -> s.internalSetActiveFileStorageService(service));
    }

    @Override
    public FileStorageService[] getAvailableFileStorageServices() {
        return serviceFinder.findAllServices().toArray(new FileStorageService[0]);
    }

    @Override
    public FileStorageService getFileStorageService(String name) {
        return serviceResolver.getFileStorageService(name);
    }

    @Override
    public void setFileStorageServiceProperty(FileStorageService service, String propertyName, String propertyValue)
            throws NoCorrespondingServiceRegisteredException, IllegalArgumentException {
        apply(s -> s.internalSetFileStorageServiceProperty(service, propertyName, propertyValue));
    }
    
    @Override
    public void onServiceAdded(FileStorageService service) {
        logger.info("Found new FileStorageService: adding properties to " + service.getName());
        for (Entry<String, String> property : propertyStore.readAllProperties(service.getName()).entrySet()) {
            try {
                service.internalSetProperty(property.getKey(), property.getValue());
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Couldn't add property "+property.getValue()+": "+e.getMessage(), e);
            }
        }
    }

    @Override
    public Void internalSetFileStorageServiceProperty(FileStorageService service, String propertyName,
            String propertyValue) throws NoCorrespondingServiceRegisteredException, IllegalArgumentException {
        propertyStore.writeProperty(service.getName(), propertyName, propertyValue);
        service.internalSetProperty(propertyName, propertyValue);
        return null;
    }

    @Override
    public Void internalSetActiveFileStorageService(FileStorageService service) {
        propertyStore.writeActiveService(service == null ? null : service.getName());
        active = service;
        return null;
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStreamResolvingAgainstFileStorageServiceResolver(is, serviceResolver, null);
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is) throws IOException, ClassNotFoundException,
            InterruptedException {
        logger.info("Initializing file storage mgmt service state from initial load");

        // use empty property store (does not connect to MongoDB) on replicas
        propertyStore = EmptyFileStorageServicePropertyStoreImpl.INSTANCE;

        // FileStorageServices are resolved against the OSGi registry by their name so that we
        // get the correct instances from the object stream
        FileStorageService activeService = (FileStorageService) is.readObject();
        logger.info("Setting active file storage service: " + activeService);
        internalSetActiveFileStorageService(activeService);

        @SuppressWarnings("unchecked")
        Map<FileStorageService, FileStorageServiceProperty[]> properties = (Map<FileStorageService, FileStorageServiceProperty[]>) is
                .readObject();
        for (FileStorageService service : properties.keySet()) {
            for (FileStorageServiceProperty property : properties.get(service)) {
                logger.info("Setting file storage service property for " + service + ": " + property);
                internalSetFileStorageServiceProperty(service, property.getName(), property.getValue());
            }
        }
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream os) throws IOException {
        os.writeObject(active);

        // The FileStorageService keys of the map are resolved on the replica by the stream resolving
        // against the OSGi registry. This means the property values would be lost, which is why they
        // are added separately in this map.
        Map<FileStorageService, FileStorageServiceProperty[]> properties = new HashMap<>();
        for (FileStorageService service : getAvailableFileStorageServices()) {
            properties.put(service, service.getProperties());
        }
        os.writeObject(properties);
    }

    @Override
    public void addOperationExecutionListener(
            OperationExecutionListener<ReplicableFileStorageManagementService> listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(
            OperationExecutionListener<ReplicableFileStorageManagementService> listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public Iterable<OperationExecutionListener<ReplicableFileStorageManagementService>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }
    
    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<ReplicableFileStorageManagementService, ?> operationWithResultWithIdWrapper) {
        operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }
    
    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<ReplicableFileStorageManagementService, ?> operation) {
        return operationsSentToMasterForReplication.remove(operation);
    }
    
    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        //don't need to clear anything - only know about file storage services, and these will live on anyway
    }
    
    @Override
    public Serializable getId() {
        return getClass().getName();
    }
    
    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return replicationMasterDescriptor;
    }
    
    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        replicationMasterDescriptor = master;
    }
    
    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        replicationMasterDescriptor = null;
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
    public void setUnsentOperationToMasterSender(UnsentOperationsToMasterSender service) {
        this.unsentOperationForMasterQueue = service;
    }

    @Override
    public <S, O extends OperationWithResult<S, ?>, T> void retrySendingLater(
            OperationWithResult<S, T> operationWithResult, OperationsToMasterSender<S, O> sender) {
        if (unsentOperationForMasterQueue != null) {
            unsentOperationForMasterQueue.retrySendingLater(operationWithResult, sender);
        }
    }
}
