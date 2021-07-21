package com.sap.sse.security.ui.client.premium;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;
import com.sap.sse.security.ui.client.subscription.SubscriptionWriteServiceAsync;

public class PayWallResolver {

    private final UserService userService;
    private final SubscriptionServiceFactory subscriptionServiceFactory;
    private final SecuredDTO dtoContext;

    public PayWallResolver(UserService userService, SubscriptionServiceFactory subscriptionServiceFactory, SecuredDTO dtoContext){
        this.userService = userService;
        this.subscriptionServiceFactory = subscriptionServiceFactory;
        this.dtoContext = dtoContext;
    }
    
    public boolean isPermitted(HasPermissions.Action action){
        if(dtoContext != null) {
            return userService.hasPermission(dtoContext, action);
        }else {
            return false;
        }
    }

    public boolean hasPermission(Action action) {
        return userService.hasPermission(dtoContext, action);
    }
    
    public SubscriptionWriteServiceAsync<?, ?, ?> getSubscriptionWriteService() {
        try {
            return subscriptionServiceFactory.getDefaultWriteAsyncService();
        } catch (InvalidSubscriptionProviderException e) {
            e.printStackTrace();
            return null;
        }
    }
}
