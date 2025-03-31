package com.sap.sse.landscape.aws.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.AwsLandscapeState;
import com.sap.sse.landscape.aws.common.shared.SecuredAwsLandscapeType;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.replication.Replicable;
import com.sap.sse.security.PermissionChangeListener;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissionsProvider;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * When the {@link #getDefaultLandscape()} method is invoked, a default
 * {@link AwsLandscape} object is created using the credentials provided in the system properties
 * {@link AwsLandscape#ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME} and
 * {@link AwsLandscape#SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME} which can be obtained using
 * {@link #getDefaultLandscape()}. At this time the landscape object reads any persistent state from the
 * persistent store.
 * <p>
 * 
 * Furthermore, this activator keeps track of changes to the set of SSH keys of those users permitted to manage the AWS
 * landscape. These are the users with the permission as described by {@link #landscapeManagerPermission} (usually
 * {@code LANDSCAPE:MANAGE:AWS}; see also {@link #AWS_LANDSCAPE_OBJECT_ID}). To keep track, this activator tracks the
 * {@link SecurityService} using the OSGi service registry and for each security service instances that shows up it
 * registers a permission change listener which updates the {@link #timePointOfLastChangeOfSetOfLandscapeManagers} each
 * time it is invoked. Furthermore, this activator registers as an {@link SSHKeyPairListener}, and each change to SSH
 * key pairs of any user part of the landscape managers at the time of the change will also update the
 * {@link #timePointOfLastChangeOfSetOfLandscapeManagers}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class Activator implements BundleActivator {
    private static final String AWS_LANDSCAPE_OBJECT_ID = "AWS";
    private final static Logger logger = Logger.getLogger(Activator.class.getName());
    private static Activator instance;

    private final WildcardPermission landscapeManagerPermission;
    private TimePoint timePointOfLastChangeOfSetOfLandscapeManagers;
    private SSHKeyPairListener sshKeyPairListener;
    private PermissionChangeListener permissionChangeListener;
    private FullyInitializedReplicableTracker<SecurityService> securityServiceTracker;
    private AwsLandscapeStateImpl landscapeState;

    public Activator() {
        super();
        this.landscapeManagerPermission = WildcardPermission.builder().withTypes(SecuredLandscapeTypes.LANDSCAPE)
                .withActions(SecuredLandscapeTypes.LandscapeActions.MANAGE).withIds(AWS_LANDSCAPE_OBJECT_ID).build();
        timePointOfLastChangeOfSetOfLandscapeManagers = TimePoint.now();
        sshKeyPairListener = new SSHKeyPairListener() {
            @Override
            public void sshKeyPairAdded(SSHKeyPair sshKeyPair) {
                timePointOfLastChangeOfSetOfLandscapeManagers = TimePoint.now();
            }
            
            @Override
            public void sshKeyPairRemoved(SSHKeyPair sshKeyPair) {
                timePointOfLastChangeOfSetOfLandscapeManagers = TimePoint.now();
            }
        };
        permissionChangeListener = (permission, usersNowHavingPermission) -> timePointOfLastChangeOfSetOfLandscapeManagers = TimePoint.now();
        landscapeState = new AwsLandscapeStateImpl();
        landscapeState.addSSHKeyPairListener(sshKeyPairListener);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        instance = this;
        context.registerService(HasPermissionsProvider.class, SecuredAwsLandscapeType::getAllInstances, null);
        securityServiceTracker = FullyInitializedReplicableTracker
                .createAndOpen(context, SecurityService.class,
                        new ServiceTrackerCustomizer<SecurityService, SecurityService>() {
                            @Override
                            public SecurityService addingService(ServiceReference<SecurityService> reference) {
                                final SecurityService securityService = context.getService(reference);
                                securityService.addPermissionChangeListener(landscapeManagerPermission,
                                        permissionChangeListener);
                                return securityService;
                            }

                            @Override
                            public void modifiedService(ServiceReference<SecurityService> reference,
                                    SecurityService service) {
                            }

                            @Override
                            public void removedService(ServiceReference<SecurityService> reference,
                                    SecurityService service) {
                                service.removePermissionChangeListener(landscapeManagerPermission,
                                        permissionChangeListener);
                            }
                        });
        new Thread(() -> {
            try {
                securityServiceTracker.getInitializedService(0).addPermissionChangeListener(landscapeManagerPermission, permissionChangeListener);
            } catch (InterruptedException e) {
                logger.warning("Problem obtaining SecurityService in " + Activator.class.getName());
            }
        }, "Waiting for SecurityService in " + Activator.class.getName()).start();
        final Dictionary<String, String> replicableServiceProperties = new Hashtable<>();
        replicableServiceProperties.put(Replicable.OSGi_Service_Registry_ID_Property_Name, landscapeState.getId().toString());
        context.registerService(Replicable.class, landscapeState, replicableServiceProperties);
    }

    public static Activator getInstance() {
        if (instance == null) {
            instance = new Activator();
        }
        return instance;
    }
    
    /**
     * Obtains the single landscape state object that this bundle instance has. Is manages the persistent, replicable state
     * of the AWS landscape, such as the set of SSH key pairs.
     */
    public AwsLandscapeState getLandscapeState() {
        return landscapeState;
    }

    public TimePoint getTimePointOfLastChangeOfSetOfLandscapeManagers() {
        return timePointOfLastChangeOfSetOfLandscapeManagers;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        landscapeState.removeSSHKeyPairListener(sshKeyPairListener);
        final SecurityService securityService = securityServiceTracker.getService();
        if (securityService != null) {
            securityService.removePermissionChangeListener(landscapeManagerPermission, permissionChangeListener);
        }
    }
}
