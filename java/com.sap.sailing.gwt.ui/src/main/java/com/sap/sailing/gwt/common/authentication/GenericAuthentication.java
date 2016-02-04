package com.sap.sailing.gwt.common.authentication;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactoryImpl;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.AuthenticationManagerImpl;
import com.sap.sse.security.ui.authentication.AuthenticationPlaceManagementController;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationPresenter;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationView;
import com.sap.sse.security.ui.client.UserService;

public class GenericAuthentication {
    private final EventBus eventBus = new SimpleEventBus();
    private final AuthenticationManager manager;
    
    public GenericAuthentication(UserService userService, AuthenticationMenuView menuView,
            FlyoutAuthenticationView display, CommonSharedResources res) {
        manager = new AuthenticationManagerImpl(userService, eventBus,
                SailingAuthenticationEntryPointLinkFactory.INSTANCE.createEmailValidationLink(),
                SailingAuthenticationEntryPointLinkFactory.INSTANCE.createPasswordResetLink());
        final AuthenticationClientFactory clientFactory = new AuthenticationClientFactoryImpl(manager, res);
        final GenericSailingAuthenticationCallbackImpl callback = new GenericSailingAuthenticationCallbackImpl();
        final AuthenticationPlaceManagementController controller = new AuthenticationPlaceManagementController(
                clientFactory, callback, display, eventBus);
        callback.setController(controller);
        new FlyoutAuthenticationPresenter(display, menuView, controller, eventBus, manager.getAuthenticationContext());
    }

    public EventBus getEventBus() {
        return eventBus;
    }
    
    public AuthenticationManager getAuthenticationManager() {
        return manager;
    }
}
