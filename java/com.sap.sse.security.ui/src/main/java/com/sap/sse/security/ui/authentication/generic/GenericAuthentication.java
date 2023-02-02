package com.sap.sse.security.ui.authentication.generic;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sse.gwt.client.mvp.ClientFactory;
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
import com.sap.sse.security.ui.client.premium.PaywallResolver;

/**
 * Configures the authentication framework in a common way that is suitable for generic flyout based authentication.
 * This can be used for cases when no specific handling of the authentication or deeper integration is necessary.
 * 
 * Typically you would wire the whole authentication mechanism in the {@link ClientFactory}. For simple modules there is
 * potentially no {@link ClientFactory}, so using this is suitable.
 */
public class GenericAuthentication {
    private final EventBus eventBus = new SimpleEventBus();
    private final AuthenticationManager manager;
    
    public GenericAuthentication(UserService userService, PaywallResolver paywallResolver, AuthenticationMenuView menuView,
            FlyoutAuthenticationView display, GenericAuthenticationLinkFactory linkFactory, CommonSharedResources res) {
        manager = new AuthenticationManagerImpl(userService, paywallResolver, eventBus,
                linkFactory.createEmailValidationLink(), linkFactory.createPasswordResetLink());
        final AuthenticationClientFactory clientFactory = new AuthenticationClientFactoryImpl(manager, res);
        final GenericAuthenticationCallbackImpl callback = new GenericAuthenticationCallbackImpl(linkFactory);
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
