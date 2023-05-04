package com.sap.sse.security.ui.server.subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.shiro.SecurityUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.replication.FullyInitializedReplicableTracker;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.SubscriptionPlan;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.subscription.SubscriptionApiService;
import com.sap.sse.security.ui.client.subscription.SubscriptionService;
import com.sap.sse.security.ui.server.Activator;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * Base class with some util methods for backend subscription remote service implementation. To setup service provider
 * regarding logic, or initialize any custom service logics, particular child implementation should override
 * {@code initService} method
 */
public abstract class SubscriptionServiceImpl extends RemoteServiceServlet implements SubscriptionService {
    private static final long serialVersionUID = -2953209842119970755L;
    private static final Logger logger = Logger.getLogger(SubscriptionServiceImpl.class.getName());

    private BundleContext context;
    private CompletableFuture<SecurityService> securityService;
    private ServiceTracker<SubscriptionApiService, SubscriptionApiService> subscriptionApiServiceTracker;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = Activator.getContext();
        securityService = initSecurityService();
        subscriptionApiServiceTracker = ServiceTrackerFactory.createAndOpen(context, SubscriptionApiService.class);
    }
    
    @Override
    public ArrayList<String> getUnlockingSubscriptionplans(WildcardPermission permission)
            throws UserManagementException {
        final ArrayList<String> result = new ArrayList<>();
        final User currentUser;
        if (getSecurityService().getCurrentUser() != null) {
            currentUser = getSecurityService().getCurrentUser();
        } else {
            currentUser = getSecurityService().getAllUser();
        }
        final SecurityService securityServiceInstance = getSecurityService();
        User allUser = getSecurityService().getAllUser();
        getSecurityService().getAllSubscriptionPlans().values().forEach((plan) -> {
            final Role[] subscriptionPlanUserRolesArray = getSecurityService().getSubscriptionPlanUserRoles(currentUser, plan);
            final Set<Role> subscriptionPlanUserRoles = Stream.of(subscriptionPlanUserRolesArray).collect(Collectors.toSet());
            List<Set<String>> parts = permission.getParts();
            if (parts.size() > 2 && !parts.get(2).isEmpty()) {
                boolean allChecksPassed = true;
                for (final QualifiedObjectIdentifier objectIdentifier : permission.getQualifiedObjectIdentifiers()) {
                    final OwnershipAnnotation ownership = securityServiceInstance.getOwnership(objectIdentifier);
                    final AccessControlListAnnotation acl = securityServiceInstance
                            .getAccessControlList(objectIdentifier);
                    allChecksPassed = PermissionChecker.isPermitted(permission, currentUser, allUser,
                            ownership == null ? null : ownership.getAnnotation(),
                            acl == null ? null : acl.getAnnotation(), subscriptionPlanUserRoles);
                    if (!allChecksPassed) {
                        break;
                    }
                }
                if(allChecksPassed) {
                    result.add(plan.getId());
                }
            } else {
                if(PermissionChecker.isPermitted(permission, currentUser, allUser, null, null, subscriptionPlanUserRoles)) {
                    result.add(plan.getId());
                }
            }
        });
        return result;
    }
    
    @Override
    public boolean isUserInPossessionOfRoles(String priceId) throws UserManagementException {
        final User currentUser = getCurrentUser();
        final SubscriptionPlan plan = getSecurityService().getSubscriptionPlanByItemPriceId(priceId);
        return plan.isUserInPossessionOfRoles(currentUser);
    }
    

    private CompletableFuture<SecurityService> initSecurityService() {
        final FullyInitializedReplicableTracker<SecurityService> tracker = FullyInitializedReplicableTracker
                .createAndOpen(context, SecurityService.class);
        return CompletableFuture.supplyAsync(() -> {
            SecurityService result = null;
            try {
                logger.info("Waiting for SecurityService...");
                result = tracker.getInitializedService(0);
                logger.info("Obtained SecurityService " + result);
                SecurityUtils.setSecurityManager(result.getSecurityManager());
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for SecurityService service", e);
            }
            return result;
        });
    }

    /**
     * Save user subscription
     */
    protected void updateUserSubscription(User user, Subscription subscription) throws UserManagementException {
        logger.info(() -> "Update user subscription, user " + user.getName() + ", new subsription "
                + subscription.toString());
        getSecurityService().updateUserSubscription(user.getName(), subscription);
    }

    protected boolean isSubscribedToMutuallyExclusivePlan(User user, SubscriptionPlan newPlan) {
        final Iterable<Subscription> subscriptions = user.getSubscriptions();
        if (subscriptions != null) {
            for (Subscription sub : subscriptions) {
                SubscriptionPlan subscribedPlan = getSecurityService().getSubscriptionPlanById(sub.getPlanId());
                if (subscribedPlan != null && isValidSubscription(sub) && !isSubscriptionCancelled(sub)
                        && Util.containsAny(subscribedPlan.getPlanCategories(), newPlan.getPlanCategories())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if subscription is valid
     */
    protected boolean isValidSubscription(Subscription subscription) {
        return subscription != null
                && !(subscription.getSubscriptionId() == null || subscription.getSubscriptionId().length() == 0);
    }

    /**
     * Util method for getting user first and last name
     */
    protected Pair<String, String> getUserFirstAndLastName(User user) {
        final Pair<String, String> result;
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            result = new Pair<>(user.getName(), "");
        } else {
            final String[] userNameParts = user.getFullName().split("\\s+");
            final String firstName = userNameParts[0];
            final String lastName;
            if (userNameParts.length > 1) {
                lastName = String.join(" ", Arrays.copyOfRange(userNameParts, 1, userNameParts.length));
            } else {
                lastName = "";
            }
            result = new Pair<>(firstName, lastName);
        }
        return result;
    }

    protected SecurityService getSecurityService() {
        final SecurityService service;
        try {
            service = securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    protected User getCurrentUser() throws UserManagementException {
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return user;
    }

    protected SubscriptionApiService getApiService() {
        ServiceReference<SubscriptionApiService>[] serviceReferences = subscriptionApiServiceTracker
                .getServiceReferences();
        for (final ServiceReference<SubscriptionApiService> serviceReference : serviceReferences) {
            if (Util.equalsWithNull(
                    serviceReference.getProperty(SubscriptionApiService.PROVIDER_NAME_OSGI_REGISTRY_KEY),
                    getProviderName())) {
                SubscriptionApiService service = context.getService(serviceReference);
                if (service.isActive()) {
                    return service;
                }
            }
        }
        return null;
    }
    
    protected SubscriptionPlanDTO convertToDto(SubscriptionPlan plan) {
        final boolean isUserSubscribedToPlan = isUserSubscribedToPlan(plan.getId());
        boolean isUserSubscribedToAllPlanCategories = false;
        boolean hasHadSubscriptionForOneTimePlan;
        if (isUserSubscribedToPlan) {
            isUserSubscribedToAllPlanCategories = true;
            hasHadSubscriptionForOneTimePlan = plan.getIsOneTimePlan();
        } else {
            for (SubscriptionPlan subscriptionPlan : getSecurityService().getAllSubscriptionPlans().values()) {
                if (isUserSubscribedToPlan(subscriptionPlan.getId())
                        && Util.containsAny(plan.getPlanCategories(), subscriptionPlan.getPlanCategories())) {
                    isUserSubscribedToAllPlanCategories = true;
                    break;
                }
            }
            try {
                final User currentUser = getCurrentUser();
                hasHadSubscriptionForOneTimePlan = currentUser.hasAnySubscription(plan.getId())
                        && plan.getIsOneTimePlan();
            } catch (UserManagementException e) {
                hasHadSubscriptionForOneTimePlan = false;
            }
        }
        final boolean disablePrice = hasHadSubscriptionForOneTimePlan;
        final Set<SubscriptionPrice> prices = new HashSet<>();
        plan.getPrices().forEach(price -> {
            price.setDisablePlan(disablePrice);
            prices.add(price);
        });
        return new SubscriptionPlanDTO(plan.getId(), isUserSubscribedToPlan, prices, plan.getPlanCategories(),
                hasHadSubscriptionForOneTimePlan, isUserSubscribedToAllPlanCategories, null, plan.getGroup());
    }

    private boolean isUserSubscribedToPlan(String planId) {
        try {
            final User currentUser = getCurrentUser();
            final Subscription subscriptionByPlan = currentUser.getSubscriptionByPlan(planId);
            return subscriptionByPlan != null ? subscriptionByPlan.isActiveSubscription() : false;
        } catch (UserManagementException e) {
            return false;
        }
    }
    
    protected ArrayList<SubscriptionPlanDTO> convertToDtos(Collection<SubscriptionPlan> plans) {
        return plans.stream().map((plan) -> convertToDto(plan)).collect(Collectors.toCollection(ArrayList::new));
    }
    
    protected abstract String getProviderName();
    
    protected abstract boolean isSubscriptionCancelled(Subscription subscription);
}
