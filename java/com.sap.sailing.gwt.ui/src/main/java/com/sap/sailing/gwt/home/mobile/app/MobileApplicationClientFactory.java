package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sailing.gwt.home.desktop.app.ApplicationTopLevelView;
import com.sap.sailing.gwt.home.mobile.places.error.ErrorViewImpl;
import com.sap.sailing.gwt.home.mobile.places.searchresult.SearchResultViewImpl;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.partials.busy.BusyViewImpl;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultClientFactory;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationView;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationViewImpl;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetView;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContext;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContextImpl;
import com.sap.sailing.gwt.ui.client.refresh.BusyView;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.security.ui.client.DefaultWithSecurityImpl;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * 
 * @author pgtaboada
 *
 */
public class MobileApplicationClientFactory extends
        SecureClientFactoryImpl<ApplicationTopLevelView<ResettableNavigationPathDisplay>> implements
        ErrorAndBusyClientFactory, SearchResultClientFactory, UserManagementClientFactory, PasswordResetClientFactory {
    private final MobilePlacesNavigator navigator;
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();
    private WithSecurity securityProvider;
    private UserManagementContext uCtx = new UserManagementContextImpl();

    public MobileApplicationClientFactory(boolean isStandaloneServer) {
        this(new SimpleEventBus(), isStandaloneServer);
    }

    private MobileApplicationClientFactory(SimpleEventBus eventBus, boolean isStandaloneServer) {
        this(eventBus, new PlaceController(eventBus), isStandaloneServer);
    }

    private MobileApplicationClientFactory(EventBus eventBus, PlaceController placeController, boolean isStandaloneServer) {
        this(eventBus, placeController, new MobilePlacesNavigator(placeController, isStandaloneServer));
    }

    private MobileApplicationClientFactory(EventBus eventBus, PlaceController placeController, MobilePlacesNavigator navigator) {
        this(new MobileApplicationView(navigator, eventBus), eventBus, placeController, navigator);
    }

    public MobileApplicationClientFactory(MobileApplicationView root, EventBus eventBus,
            PlaceController placeController, final MobilePlacesNavigator navigator) {
        super(root, eventBus, placeController);
        this.navigator = navigator;
        securityProvider = new DefaultWithSecurityImpl();
        securityProvider.getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                uCtx = new UserManagementContextImpl(user);
                getEventBus().fireEvent(new UserManagementContextEvent(uCtx));
            }
        });
        getEventBus().addHandler(UserManagementRequestEvent.TYPE, new UserManagementRequestEvent.Handler() {
            @Override
            public void onUserManagementRequestEvent(UserManagementRequestEvent event) {
                if (!event.isLogin()) {
                    getUserManagementService().logout(new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            didLogout();
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            didLogout();
                        }
                    });
                }
            }
        });
    }

    public MobilePlacesNavigator getNavigator() {
        return navigator;
    }

    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public Place getDefaultPlace() {
        return new StartPlace();
    }

    @Override
    public BusyView createBusyView() {
        return new BusyViewImpl();
    }

    @Override
    public ErrorView createErrorView(final String errorMessage, final Throwable errorReason) {
        return new ErrorViewImpl(errorMessage, errorReason, null);
    }
    
    @Override
    public SearchResultView createSearchResultView() {
        return new SearchResultViewImpl(navigator);
    }

    public ResettableNavigationPathDisplay getNavigationPathDisplay() {
        return getTopLevelView().getNavigationPathDisplay();
    }

    @Override
    public UserManagementContext getUserManagementContext() {
        return uCtx;
    }
    
    @Override
    public void didLogout() {
        uCtx = new UserManagementContextImpl();
        securityProvider.getUserService().updateUser(true);
        getEventBus().fireEvent(new UserManagementRequestEvent());
    }

    @Override
    public void didLogin(UserDTO user) {
        uCtx = new UserManagementContextImpl(user);
        securityProvider.getUserService().updateUser(true);
        getEventBus().fireEvent(new UserManagementContextEvent(uCtx));
    }
    
    public void refreshUser() {
        getUserManagementService().getCurrentUser(new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                didLogin(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public UserManagementServiceAsync getUserManagement() {
        return securityProvider.getUserManagementService();
    }
    
    @Override
    public ConfirmationView createAccountConfirmationView() {
        return new ConfirmationViewImpl(SharedResources.INSTANCE, StringMessages.INSTANCE.accountConfirmation());
    }
    
    @Override
    public PasswordResetView createPasswordResetView() {
        return new PasswordResetViewImpl();
    }
    
    @Override
    public PlaceNavigation<ConfirmationPlace> getPasswordResettedConfirmationNavigation(String username) {
        return getNavigator().getPasswordResettedConfirmationNavigation(username);
    }
}
