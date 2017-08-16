package com.sap.sse.security.ui.client;

import java.util.logging.Logger;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.storage.client.Storage;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.mvp.ClientFactoryImpl;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * An implementation of a ClientFactory providing security services
 * @author Frank
 *
 */
public abstract class SecureClientFactoryImpl<TLV extends TopLevelView> extends ClientFactoryImpl<TLV> implements WithSecurity {
    private static final Logger log = Logger.getLogger(SecureClientFactoryImpl.class.getName());
    protected static final String STORAGE_KEY_FOR_USER_LOGIN_HINT = "sailing.ui.lastLoginOrSuppression";
    protected static final Duration SUPRESSION_DELAY = Duration.ONE_WEEK;
    
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
    
    protected void checkNewUserPopup(UserDTO user, Runnable newUserRunnable) {
        final TimePoint currentTime = MillisecondsTimePoint.now();
        if (user != null) {
            setUserLoginHintToStorage();
        } else {
            final TimePoint lastLoginOrSupression = parseLastNewUserSupression();
            if (lastLoginOrSupression == null
                    || lastLoginOrSupression.plus(SUPRESSION_DELAY).before(currentTime)) {
                newUserRunnable.run();
            } else {
                log.fine("No logininfo required, user was logged in recently, or clicked dismiss "
                        + lastLoginOrSupression + " cur " + currentTime);
            }
        }
    }

    private TimePoint parseLastNewUserSupression() {
        TimePoint lastLoginOrSupression = null;
        final Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            final String stringValue = storage.getItem(STORAGE_KEY_FOR_USER_LOGIN_HINT);
            try {
                if (stringValue != null) {
                    lastLoginOrSupression = new MillisecondsTimePoint(Long.parseLong(stringValue));
                }
            } catch (Exception e) {
                log.warning("Error parsing localstore value '" + stringValue + "'");
                storage.removeItem(STORAGE_KEY_FOR_USER_LOGIN_HINT);
            }
        }
        return lastLoginOrSupression;
    }

    protected void setUserLoginHintToStorage() {
        final Storage storage = Storage.getLocalStorageIfSupported();
        if(storage != null) {
            storage.setItem(STORAGE_KEY_FOR_USER_LOGIN_HINT, String.valueOf(MillisecondsTimePoint.now().asMillis()));
        }
    }
}
