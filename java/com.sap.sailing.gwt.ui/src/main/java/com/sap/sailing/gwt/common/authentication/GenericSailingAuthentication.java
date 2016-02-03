package com.sap.sailing.gwt.common.authentication;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactoryImpl;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.AuthenticationManagerImpl;
import com.sap.sse.security.ui.authentication.AuthenticationPlaceManagementController;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationPresenter;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationView;
import com.sap.sse.security.ui.client.UserService;

public class GenericSailingAuthentication {
    private static final CommonSharedResources res = CommonSharedResources.INSTANCE;
    
    public GenericSailingAuthentication(UserService userService, AuthenticationMenuView userManagementMenuView) {
        res.mainCss().ensureInjected();
        
        EventBus eventBus = new SimpleEventBus();
        FlyoutAuthenticationView display = new GenericSailingAuthenticationView(res);
        AuthenticationManager manager = new AuthenticationManagerImpl(userService, eventBus,
                SailingAuthenticationEntryPointLinkFactory.createEmailValidationLink(), 
                SailingAuthenticationEntryPointLinkFactory.createPasswordResetLink());
        AuthenticationClientFactory clientFactory = new AuthenticationClientFactoryImpl(manager, res);
        WrappedPlaceManagementController userManagementController = null;
        AuthenticationCallback callback = new GenericSailingAuthenticationCallbackImpl(userManagementController);
        userManagementController = new AuthenticationPlaceManagementController(clientFactory, callback, display, eventBus);
        new FlyoutAuthenticationPresenter(display, userManagementMenuView, userManagementController, eventBus, manager.getAuthenticationContext());
    }

}
