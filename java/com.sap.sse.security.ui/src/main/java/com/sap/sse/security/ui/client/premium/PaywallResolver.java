package com.sap.sse.security.ui.client.premium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.subscription.SubscriptionClientProvider;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteService;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;

/**
 * Paywall Resolver is needed to offer all services and functions related to the paywall (Chargebee).
 * On initialization also the paywall providers will be initiated.
 */
public class PaywallResolver {

    private static Logger LOG = Logger.getLogger(PaywallResolver.class.getName());

    private final UserService userService;
    private final SubscriptionServiceFactory subscriptionServiceFactory;

    /**
     * See bug5696.
     */
    private boolean isDisabled;

    /**
     * The registered provider from subscription service factory will be initialized.
     * 
     * @param userService
     *            user service to provide permission check and get user change event.
     * @param subscriptionServiceFactory
     *            subscription service factory to handle providers and provide subscription service.
     */
    public PaywallResolver(final UserService userService, final SubscriptionServiceFactory subscriptionServiceFactory) {
        this.userService = userService;
        this.subscriptionServiceFactory = subscriptionServiceFactory;
    }

    public void getUnlockingSubscriptionPlans(final Action action, final SecuredDTO dtoContext,
            final Consumer<List<String>> callback) {
        final WildcardPermission permission = dtoContext == null
                ? WildcardPermission.builder().withActions(action).build()
                : dtoContext.getIdentifier().getPermission(action);
        getSubscriptionWriteService().getUnlockingSubscriptionplans(permission, new AsyncCallback<ArrayList<String>>() {

            @Override
            public void onSuccess(final ArrayList<String> result) {
                callback.accept(result);
            }

            @Override
            public void onFailure(final Throwable caught) {
                LOG.warning("Unable to determine subscription plans unlocking action " + action.name());
                callback.accept(Collections.emptyList());
            }
        });
    }

    /**
     * Get the permission based on the given action and secured DTO context object. If the disabled flag (Bug 5696
     * workaraound) is set on {@link PaywallResolver} the result will always be TRUE. If no action is set the default
     * {@link DefaultActions}.READ will be used. From the {@link UserService} there is the default behavior, that if the
     * context object is NULL the result is set to FALSE.
     * 
     * @param action
     *            The action on which the permission check is based on.
     * @param dtoContext
     *            the secured DTO context on which the permission check is based on.
     * @return the permission check result
     */
    public boolean hasPermission(final Action action, final SecuredDTO dtoContext) {
        if (isDisabled) {
            return true;
        } else if (action != null) {
            return userService.hasPermission(dtoContext, action);
        } else {
            return userService.hasPermission(dtoContext, DefaultActions.READ);
        }
    }

    /**
     * Get all HasPermissions of a set of actions.
     * 
     * @param premiumActions
     *            A set of premium actions.
     * @param dtoContext
     *            the context DTO (SecuredDTO)
     * 
     * @return a Map of HasPermissions results based on a set of premium {@link Action}s.
     */
    public Map<Action, Boolean> getHasPermissionMap(final Set<Action> premiumActions, final SecuredDTO dtoContext) {
        final Map<Action, Boolean> premiumPermissions = new HashMap<>();
        for (Action premiumAction : premiumActions) {
            premiumPermissions.put(premiumAction, this.hasPermission(premiumAction, dtoContext));
        }
        return premiumPermissions;
    }

    /**
     * Getter of {@link SubscriptionWriteServiceAsync}. In case of an {@link InvalidSubscriptionProviderException} null
     * will be returned.
     * 
     * @return the async version of {@link SubscriptionWriteService}.
     */
    public SubscriptionWriteServiceAsync<?, ?, ?> getSubscriptionWriteService() {
        try {
            return subscriptionServiceFactory.getDefaultWriteAsyncService();
        } catch (final InvalidSubscriptionProviderException e) {
            LOG.log(Level.SEVERE,
                    "An invalid subscription provider exception occured while getting the SubscriptionWriteSerivce.",
                    e);
            return null;
        }
    }

    /**
     * Getter of {@link SubscriptionClientProvider}. In case of {@link InvalidSubscriptionProviderException} null will
     * be returned.
     * 
     * @return the default provider.
     */
    public SubscriptionClientProvider getSubscriptionClientProvider() {
        try {
            return subscriptionServiceFactory.getDefaultProvider();
        } catch (final InvalidSubscriptionProviderException e) {
            LOG.log(Level.SEVERE,
                    "An invalid subscription provider exception occured while getting the default provider.", e);
            return null;
        }
    }

    /**
     * It is possible to register an event handler to get the user status change event and react on this.
     * 
     * @param handler
     *            an implementation of the {@link UserStatusEventHandler}.
     * @return the registration object.
     */
    public HandlerRegistration registerUserStatusEventHandler(final UserStatusEventHandler handler) {
        userService.addUserStatusEventHandler(handler);
        return () -> userService.removeUserStatusEventHandler(handler);
    }

    /**
     * TODO bug5696: When bug is resolved, this workaround is no longer necessary.
     * 
     * @param isDisabled
     */
    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }
}
