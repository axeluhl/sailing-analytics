package com.sap.sse.security.ui.client;

import java.util.Date;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.storage.client.Storage;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.security.ui.authentication.login.LoginPopup;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * An implementation of a ClientFactory providing security services
 * @author Frank
 *
 */
public abstract class SecureClientFactoryImpl<TLV extends TopLevelView> extends ClientFactoryImpl<TLV> implements WithSecurity {
    protected static final String STORAGE_KEY_FOR_USER_LOGIN_HINT = "lastnewUserSupression";
    protected static final long SUPRESSION_DELAY = 1000*60*60*24*7;
    
    private WithSecurity securityProvider;

    public SecureClientFactoryImpl(TLV root) {
        this(root, new SimpleEventBus());
    }
    
    protected SecureClientFactoryImpl(TLV root, EventBus eventBus) {
        this(root, eventBus, new PlaceController(eventBus));
    }
    
    protected SecureClientFactoryImpl(TLV root, EventBus eventBus, PlaceController placeController) {
        super(root, eventBus, placeController);
        
        securityProvider = new DefaultWithSecurityImpl();
    }

    @Override
    public UserManagementServiceAsync getUserManagementService() {
        return securityProvider.getUserManagementService();
    }
    
    @Override
    public UserService getUserService() {
        return securityProvider.getUserService();
    }
    
    protected void checkNewUserPopup(UserDTO user, boolean desktop, Runnable gotoMoreInfo) {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            Date currentTime = new Date();
            if (user != null) {
                storage.setItem(STORAGE_KEY_FOR_USER_LOGIN_HINT, String.valueOf(currentTime.getTime()));
            } else {
                Date lastLoginOrSupression = null;
                try {
                    String value = storage.getItem(STORAGE_KEY_FOR_USER_LOGIN_HINT);
                    if (value != null) {
                        lastLoginOrSupression = new Date(Long.parseLong(value));
                    }
                } catch (Exception e) {
                    GWT.log("Error parsing localstore value!");
                    storage.removeItem(STORAGE_KEY_FOR_USER_LOGIN_HINT);
                }
                if (lastLoginOrSupression == null
                        || lastLoginOrSupression.getTime() + SUPRESSION_DELAY < currentTime.getTime()) {
                    new LoginPopup(desktop, () -> {
                        storage.setItem(STORAGE_KEY_FOR_USER_LOGIN_HINT, String.valueOf(currentTime.getTime()));
                    }, () -> {
                        storage.setItem(STORAGE_KEY_FOR_USER_LOGIN_HINT, String.valueOf(currentTime.getTime()));
                        gotoMoreInfo.run();
                    }).show();
                } else {
                    GWT.log("No logininfo required, user was logged in recently, or clicked dismiss "
                            + lastLoginOrSupression + " cur " + currentTime);
                }
            }

        }
    }
}
