package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.SimplePrincipalCollection;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;
import com.sap.sailing.domain.tracking.AbstractWindTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

public class IgtimiWindTracker extends AbstractWindTracker implements WindTracker {
    private static final Logger logger = Logger.getLogger(IgtimiWindTracker.class.getName());
    private static final int TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS = 10*60*1000; // 10 minutes
    private static final long TIME_INTERVAL_TO_TRACK_AFTER_END_OF_RACE_MILLIS = 60*60*1000; // 60 minutes
    private final Map<LiveDataConnection, Util.Triple<Iterable<String>, Account, IgtimiWindReceiver>> liveConnectionsAndDeviceSerialNumber;
    private final IgtimiWindTrackerFactory windTrackerFactory;
    private boolean stopping;

    protected IgtimiWindTracker(final DynamicTrackedRace trackedRace, final IgtimiConnectionFactory connectionFactory,
            final IgtimiWindTrackerFactory windTrackerFactory, final boolean correctByDeclination,
            SecurityService optionalSecurityService) throws Exception {
        super(trackedRace);
        this.windTrackerFactory = windTrackerFactory;
        liveConnectionsAndDeviceSerialNumber = new HashMap<>();
        new Thread("IgtimiWindTracker start-up thread for race " + trackedRace.getRace().getName()) {
            public void run() {
                logger.info("Starting up Igtimi wind tracker for race "+trackedRace.getRace().getName());
                // avoid a race condition with stop() being called while this start-up thread is still running
                synchronized (IgtimiWindTracker.this) {
                    Iterable<Account> accounts = connectionFactory.getAllAccounts();
                    for (Account account : accounts) {
                        if (isPermittedToUseAccount(optionalSecurityService, trackedRace, account)) {
                            try {
                                if (!stopping) {
                                    IgtimiConnection connection = connectionFactory.connect(account);
                                    Iterable<String> devicesWeShouldListenTo = connection.getWindDevices();
                                    if (!stopping) {
                                        LiveDataConnection liveConnection = connection.getOrCreateLiveConnection(devicesWeShouldListenTo);
                                        IgtimiWindReceiver windReceiver = new IgtimiWindReceiver(correctByDeclination ? DeclinationService.INSTANCE : null);
                                        liveConnection.addListener(windReceiver);
                                        windReceiver.addListener(new WindListenerSendingToTrackedRace(Collections.singleton(getTrackedRace()), windTrackerFactory));
                                        liveConnectionsAndDeviceSerialNumber.put(liveConnection, new Util.Triple<Iterable<String>, Account, IgtimiWindReceiver>(
                                                devicesWeShouldListenTo, account, windReceiver));
                                    }
                                }
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Exception trying to start Igtimi wind tracker for race "
                                        + getTrackedRace().getRace().getName() + " for account " + account);
                            }
                        }
                    }
                }
            }

        }.start();
    }

    private boolean isPermittedToUseAccount(SecurityService optionalSecurityService,
            final DynamicTrackedRace trackedRace, Account account) {
        final boolean isPermittedToUseAccount;
        final WildcardPermission permissionToCheck = account.getIdentifier().getPermission(DefaultActions.READ);
        final String stringPermissionToCheck = permissionToCheck.toString();
        if (optionalSecurityService == null || SecurityUtils.getSubject().isPermitted(stringPermissionToCheck)) {
            isPermittedToUseAccount = true;
        } else if (!SecurityUtils.getSubject().isAuthenticated()) {
            // This is most probably a server reload where no security information is available
            final OwnershipAnnotation ownershipOfRace = optionalSecurityService.getOwnership(trackedRace.getIdentifier());
            final User userOwnerOfRace = ownershipOfRace == null ? null
                    : ownershipOfRace.getAnnotation().getUserOwner();
            if (userOwnerOfRace != null && SecurityUtils.getSecurityManager().isPermitted(
                    new SimplePrincipalCollection(userOwnerOfRace.getName(), userOwnerOfRace.getName()),
                    stringPermissionToCheck)) {
                // The user owner of the TrackedRace would be permitted to use this Account
                isPermittedToUseAccount = true;
            } else {
                final UserGroup groupOwner = ownershipOfRace == null ? null
                        : ownershipOfRace.getAnnotation().getTenantOwner();
                if (groupOwner != null) {
                    if (groupOwner.equals(optionalSecurityService.getDefaultTenant())) {
                        // It is assumed to be an auto-migration case
                        isPermittedToUseAccount = true;
                    } else {
                        final User allUser = optionalSecurityService.getAllUser();
                        Iterable<UserGroup> userGroupsOfAllUser = allUser == null ? Collections.emptySet()
                                : optionalSecurityService.getUserGroupsOfUser(allUser);
                        final OwnershipAnnotation ownershipOfAccount = optionalSecurityService
                                .getOwnership(account.getIdentifier());
                        final AccessControlListAnnotation aclOfAccount = optionalSecurityService
                                .getAccessControlList(account.getIdentifier());
                        // Checks if the permission would be granted by the group owner 
                        if (PermissionChecker.isPermitted(permissionToCheck, null, Collections.singleton(groupOwner),
                                allUser, userGroupsOfAllUser,
                                ownershipOfAccount == null ? null : ownershipOfAccount.getAnnotation(),
                                aclOfAccount == null ? null : aclOfAccount.getAnnotation())) {
                            isPermittedToUseAccount = true;
                        } else {
                            isPermittedToUseAccount = false;
                        }
                    }
                } else {
                    isPermittedToUseAccount = false;
                }
            }
        } else {
            isPermittedToUseAccount = false;
        }
        return isPermittedToUseAccount;
    }

    public static TimePoint getReceivingEndTime(DynamicTrackedRace trackedRace) {
        TimePoint endOfRace = trackedRace.getEndOfRace();
        TimePoint endOfTracking = trackedRace.getEndOfTracking();
        final TimePoint endTime;
        if (endOfRace == null) {
            if (endOfTracking == null) {
                endTime = null;
            } else {
                endTime = endOfTracking.plus(TIME_INTERVAL_TO_TRACK_AFTER_END_OF_RACE_MILLIS);
            }
        } else {
            if (endOfTracking == null) {
                endTime = endOfRace;
            } else {
                endTime = Collections.max(Arrays.asList(endOfRace, endOfTracking));
            }
        }
        return endTime;
    }

    /**
     * Based on the tracked race's time parameters and the current time, decides for a good start time for receiving
     * live wind data. Get all devices that have been sending during the last 10 minutes before start of race, start of
     * tracking or now, whichever came first.
     */
    public static TimePoint getReceivingStartTime(DynamicTrackedRace trackedRace) {
        List<TimePoint> startCandidates = new ArrayList<>();
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        if (startOfRace != null) {
            startCandidates.add(startOfRace);
        }
        final TimePoint startOfTracking = trackedRace.getStartOfTracking();
        if (startOfTracking != null) {
            startCandidates.add(startOfTracking);
        }
        startCandidates.add(MillisecondsTimePoint.now());
        return Collections.min(startCandidates).minus(TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS);
    }

    @Override
    public void stop() {
        logger.info("Stopping Igtimi wind tracker for race "+getTrackedRace().getRace().getName());
        stopping = true; // in case stop() is called while the start-up thread is still running, avoid more connections to be created
        // this synchronizes with the start-up thread and waits until that has finished and no longer writes to liveConnectionsAndDeviceSerialNumber
        synchronized (this) {
            windTrackerFactory.windTrackerStopped(getTrackedRace().getRace(), this);
            for (LiveDataConnection ldc : liveConnectionsAndDeviceSerialNumber.keySet()) {
                final Util.Triple<Iterable<String>, Account, IgtimiWindReceiver> deviceSerialNumberAndAccountAndReceiver = liveConnectionsAndDeviceSerialNumber.get(ldc);
                try {
                    logger.info("Stopping Igtimi live connection "+ldc);
                    ldc.stop(); // does reference counting and stops the live connection only if no other client is active anymore
                    ldc.removeListener(deviceSerialNumberAndAccountAndReceiver.getC());
                } catch (Exception e) {
                    logger.log(Level.INFO,
                            "Exception trying to stop Igtimi live connection for wind receiver for race "
                                    + getTrackedRace().getRace() + " and device " + deviceSerialNumberAndAccountAndReceiver.getA()
                                    + " in account " + deviceSerialNumberAndAccountAndReceiver.getB());
                }
            }
        }
    }
}
