package com.sap.sse.landscape.aws.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.SecuredAwsLandscapeType;
import com.sap.sse.landscape.aws.impl.SSHKeyPairListenersImpl.SSHKeyPairListener;
import com.sap.sse.landscape.aws.persistence.PersistenceFactory;
import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.PermissionChangeListener;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissionsProvider;
import com.sap.sse.security.shared.WildcardPermission;

/**
 * When the bundle is activated and this activator's {@link #start(BundleContext)} method is invoked, a default
 * {@link AwsLandscape} object is created using the credentials provided in the system properties
 * {@link AwsLandscape#ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME} and
 * {@link AwsLandscape#SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME} which can be obtained using
 * {@link #getDefaultLandscape()}. This landscape object is registered with the OSGi service registry under the
 * {@link AwsLandscape} interface.
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

    private AwsLandscapeImpl<?, ApplicationProcessMetrics, ?> landscape;
    private final WildcardPermission landscapeManagerPermission;
    private TimePoint timePointOfLastChangeOfSetOfLandscapeManagers;

    public Activator() {
        super();
        this.landscapeManagerPermission = WildcardPermission.builder().withTypes(SecuredLandscapeTypes.LANDSCAPE)
                .withActions(SecuredLandscapeTypes.LandscapeActions.MANAGE).withIds(AWS_LANDSCAPE_OBJECT_ID).build();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        instance = this;
        timePointOfLastChangeOfSetOfLandscapeManagers = TimePoint.now();
        if (System.getProperty(AwsLandscape.ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME) != null
                || System.getProperty(AwsLandscape.SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME) == null) {
            logger.info("Not all system properties of " + AwsLandscape.ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME + " and "
                    + AwsLandscape.SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME + " set. Not activating AWS landscape.");
            landscape = null;
        } else {
            landscape = new AwsLandscapeImpl<>(System.getProperty(AwsLandscape.ACCESS_KEY_ID_SYSTEM_PROPERTY_NAME),
                    System.getProperty(AwsLandscape.SECRET_ACCESS_KEY_SYSTEM_PROPERTY_NAME),
                    PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                    PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory());
            context.registerService(AwsLandscape.class, landscape, null);
        }
        context.registerService(HasPermissionsProvider.class, SecuredAwsLandscapeType::getAllInstances, null);
        final PermissionChangeListener permissionChangeListener = (permission,
                usersNowHavingPermission) -> timePointOfLastChangeOfSetOfLandscapeManagers = TimePoint.now();
        // obtain SecurityService and track so that each time a SecurityService appears, this activator can
        // register a PermissionChangeListener for the LANDSCAPE:MANAGE:AWS permission:
        final FullyInitializedReplicableTracker<SecurityService> securityServiceTracker = FullyInitializedReplicableTracker
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
                securityServiceTracker.getInitializedService(0).addPermissionChangeListener(landscapeManagerPermission,
                        permissionChangeListener);
            } catch (InterruptedException e) {
                logger.warning("Problem obtaining SecurityService in " + Activator.class.getName());
            }
        }, "Waiting for SecurityService in " + Activator.class.getName()).start();
    }

    public static Activator getInstance() {
        return instance;
    }

    public AwsLandscapeImpl<?, ApplicationProcessMetrics, ?> getDefaultLandscape() {
        return landscape;
    }

    public TimePoint getTimePointOfLastChangeOfSetOfLandscapeManagers() {
        return timePointOfLastChangeOfSetOfLandscapeManagers;
    }

    public void setLandscape(AwsLandscapeImpl<?, ApplicationProcessMetrics, ?> landscape) {
        this.landscape = landscape;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
