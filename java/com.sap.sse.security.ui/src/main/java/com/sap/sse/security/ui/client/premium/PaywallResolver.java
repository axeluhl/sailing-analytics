package com.sap.sse.security.ui.client.premium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.subscription.SubscriptionClientProvider;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;
import com.sap.sse.security.ui.server.EssentialSecuredDTOByName;
import com.sap.sse.security.ui.server.SecurityDTOUtil;

public class PaywallResolver {

    private static Logger LOG = Logger.getLogger(PaywallResolver.class.getName());

    private final UserService userService;
    private final SubscriptionServiceFactory subscriptionServiceFactory;
    private SecuredDTO dtoContext;

    private String name;
    private HasPermissions permissionType;

    public PaywallResolver(final UserService userService, final SubscriptionServiceFactory subscriptionServiceFactory,
            final SecuredDTO dtoContext) {
        this.userService = userService;
        this.subscriptionServiceFactory = subscriptionServiceFactory;
        subscriptionServiceFactory.initializeProviders();
        this.dtoContext = dtoContext;
    }

    public PaywallResolver(final UserService userService, final SubscriptionServiceFactory subscriptionServiceFactory, String name, HasPermissions permissionType) {
        this.userService = userService;
        this.subscriptionServiceFactory = subscriptionServiceFactory;
        this.name = name;
        this.permissionType = permissionType;
        subscriptionServiceFactory.initializeProviders();
    }

    public void getUnlockingSubscriptionPlans(final Action action, final Consumer<List<String>> callback) {
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

    public boolean hasPermission(final Action action) {
        if (dtoContext == null && name != null && permissionType != null) {
            dtoContext = new EssentialSecuredDTOByName(name, permissionType);
            SecurityDTOUtil.addSecurityInformation(null, dtoContext);
        }
        return userService.hasPermission(dtoContext, action);
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
    
    public void setDtoContext(SecuredDTO dtoContext) {
        this.dtoContext = dtoContext;
    }
}
