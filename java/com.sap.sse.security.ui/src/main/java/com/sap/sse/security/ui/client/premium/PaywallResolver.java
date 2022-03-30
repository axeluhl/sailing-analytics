package com.sap.sse.security.ui.client.premium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;

public class PaywallResolver {

    private static Logger LOG = Logger.getLogger(PaywallResolver.class.getName());

    private final UserService userService;
    private final SubscriptionServiceFactory subscriptionServiceFactory;
    private boolean isDisabled;

    public PaywallResolver(final UserService userService, final SubscriptionServiceFactory subscriptionServiceFactory) {
        this.userService = userService;
        this.subscriptionServiceFactory = subscriptionServiceFactory;
        subscriptionServiceFactory.initializeProviders();
    }

    public void getUnlockingSubscriptionPlans(final Action action, final SecuredDTO dtoContext,
            final Consumer<List<String>> callback) {
        final WildcardPermission permission = dtoContext == null
                ? WildcardPermission.builder().withActions(action).build()
                : dtoContext.getIdentifier().getPermission(action);
        getSubscriptionWriteService().getUnlockingSubscriptionplans(permission,
                new AsyncCallback<ArrayList<String>>() {

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

    public boolean hasPermission(final Action action, final SecuredDTO dtoContext) {
        if(isDisabled) {
            return true;
        }else if(action != null) {
            return userService.hasPermission(dtoContext, action);
        }else {
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

    public SubscriptionWriteServiceAsync<?, ?, ?> getSubscriptionWriteService() {
        try {
            return subscriptionServiceFactory.getDefaultWriteAsyncService();
        } catch (final InvalidSubscriptionProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

    public SubscriptionClientProvider getSubscriptionClientProvider() {
        try {
            return subscriptionServiceFactory.getDefaultProvider();
        } catch (final InvalidSubscriptionProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HandlerRegistration registerUserStatusEventHandler(final UserStatusEventHandler handler) {
        userService.addUserStatusEventHandler(handler);
        return () -> userService.removeUserStatusEventHandler(handler);
    }

    /**
     * TODO Bug 5696 WHen bug is resolved, this workaround is no longer necessary.
     * @param isDisabled
     */
    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }
}
