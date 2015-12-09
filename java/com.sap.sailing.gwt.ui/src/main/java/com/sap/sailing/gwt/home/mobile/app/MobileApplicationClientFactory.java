package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sailing.gwt.home.desktop.app.ApplicationTopLevelView;
import com.sap.sailing.gwt.home.mobile.places.error.ErrorViewImpl;
import com.sap.sailing.gwt.home.mobile.places.searchresult.SearchResultViewImpl;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementContext;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.app.ResettableNavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;
import com.sap.sailing.gwt.home.shared.app.UserManagementContextImpl;
import com.sap.sailing.gwt.home.shared.partials.busy.BusyViewImpl;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultClientFactory;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.ui.client.refresh.BusyView;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.security.ui.client.DefaultWithSecurityImpl;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * 
 * @author pgtaboada
 *
 */
public class MobileApplicationClientFactory extends
        SecureClientFactoryImpl<ApplicationTopLevelView<ResettableNavigationPathDisplay>> implements
        ErrorAndBusyClientFactory, SearchResultClientFactory, ClientFactoryWithUserManagementContext,
        ClientFactoryWithUserManagementService {
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
    public UserManagementServiceAsync getUserManagement() {
        return securityProvider.getUserManagementService();
    }
}
