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
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteService;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;

public class PaywallResolverImpl implements PaywallResolver {

    private static Logger LOG = Logger.getLogger(PaywallResolverImpl.class.getName());

    private final UserService userService;
    private final SubscriptionServiceFactory subscriptionServiceFactory;
    
    public PaywallResolverImpl(final UserService userService, final SubscriptionServiceFactory subscriptionServiceFactory) {
        this.userService = userService;
        this.subscriptionServiceFactory = subscriptionServiceFactory;
    }

    @Override
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
     * {@inheritDoc} Additionally, the @UserService will return FALSE if the given SecuredDTO is null.
     */
    @Override
    public boolean hasPermission(final Action action, final SecuredDTO dtoContext) {
        if (action != null) {
            return userService.hasPermission(dtoContext, action);
        } else {
            return userService.hasPermission(dtoContext, DefaultActions.READ);
        }
    }

    @Override
    public Map<Action, Boolean> getHasPermissionMap(final Set<Action> premiumActions, final SecuredDTO dtoContext) {
        final Map<Action, Boolean> premiumPermissions = new HashMap<>();
        for (Action premiumAction : premiumActions) {
            premiumPermissions.put(premiumAction, this.hasPermission(premiumAction, dtoContext));
        }
        return premiumPermissions;
    }

    @Override
    public HandlerRegistration registerUserStatusEventHandler(final UserStatusEventHandler handler) {
        userService.addUserStatusEventHandler(handler);
        return () -> userService.removeUserStatusEventHandler(handler);
    }
    
    /**
     * Getter of {@link SubscriptionWriteServiceAsync}. In case of an {@link InvalidSubscriptionProviderException} null
     * will be returned.
     * 
     * @return the async version of {@link SubscriptionWriteService}.
     */
    private SubscriptionWriteServiceAsync<?, ?, ?> getSubscriptionWriteService() {
        try {
            return subscriptionServiceFactory.getDefaultWriteAsyncService();
        } catch (final InvalidSubscriptionProviderException e) {
            LOG.log(Level.SEVERE,
                    "An invalid subscription provider exception occured while getting the SubscriptionWriteSerivce.",
                    e);
            return null;
        }
    }

}
