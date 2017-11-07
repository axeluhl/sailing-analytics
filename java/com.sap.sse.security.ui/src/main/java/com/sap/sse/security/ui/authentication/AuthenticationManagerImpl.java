package com.sap.sse.security.ui.authentication;

import static com.sap.sse.security.shared.UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL;
import static com.sap.sse.security.shared.UserManagementException.USER_ALREADY_EXISTS;

import java.util.function.Consumer;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.AuthenticationContextImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Default implementation of {@link AuthenticationManager} interface, which delegates to the respective methods of 
 * {@link UserService} or {@link UserManagementServiceAsync} provided in via constructors. Failure handling is implemented
 * by showing an appropriate error message using the browser's default dialog box via {@link Window#alert(String)}.
 * 
 * @see #AuthenticationManagerImpl(WithSecurity, EventBus, String, String)
 * @see #AuthenticationManagerImpl(UserService, EventBus, String, String)
 */
public class AuthenticationManagerImpl implements AuthenticationManager {
    
    private final UserManagementServiceAsync userManagementService;
    private final UserService userService;
    private final EventBus eventBus;
    private final String emailConfirmationUrl;
    private final String passwordResetUrl;
    
    private final StringMessages i18n = StringMessages.INSTANCE;
    private final ErrorMessageView view = new ErrorMessageViewImpl();

    /**
     * Creates an {@link AuthenticationManagerImpl} instance based on the {@link UserService} and
     * {@link UserManagementServiceAsync} instances provided by the given {@link WithSecurity} instance.
     *      * @param userService
     *            the {@link UserService} instance to use
     * @param eventBus
     *            the {@link EventBus} instance
     * @param emailConfirmationUrl
     *            URL which is send to users to verify their email address
     * @param passwordResetUrl
     *            URL which is send to users to reset their password
     */
    public AuthenticationManagerImpl(WithSecurity clientFactory, EventBus eventBus,
            String emailConfirmationUrl, String passwordResetUrl) {
        this(clientFactory.getUserManagementService(), clientFactory.getUserService(), eventBus, emailConfirmationUrl,
                passwordResetUrl);
    }
    
    /**
     * Creates an {@link AuthenticationManagerImpl} instance based on the given {@link UserService} and its underlying
     * {@link UserManagementServiceAsync} instance.
     * 
     * @param userService
     *            the {@link UserService} instance to use
     * @param eventBus
     *            the {@link EventBus} instance
     * @param emailConfirmationUrl
     *            URL which is send to users to verify their email address
     * @param passwordResetUrl
     *            URL which is send to users to reset their password
     */
    public AuthenticationManagerImpl(UserService userService, EventBus eventBus, String emailConfirmationUrl,
            String passwordResetUrl) {
        this(userService.getUserManagementService(), userService, eventBus, emailConfirmationUrl, passwordResetUrl);
    }
    
    private AuthenticationManagerImpl(UserManagementServiceAsync userManagementService, UserService userService,
            final EventBus eventBus, String emailConfirmationUrl, String passwordResetUrl) {
        this.userManagementService = userManagementService;
        this.userService = userService;
        this.eventBus = eventBus;
        this.emailConfirmationUrl = emailConfirmationUrl;
        this.passwordResetUrl = passwordResetUrl;
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                eventBus.fireEvent(new AuthenticationContextEvent(new AuthenticationContextImpl(user)));
            }
        });
        eventBus.addHandler(AuthenticationSignOutRequestEvent.TYPE, new AuthenticationSignOutRequestEvent.Handler() {
            @Override
            public void onUserManagementSignOutRequestEvent(AuthenticationSignOutRequestEvent event) {
                    logout();
            }
        });
    }

    @Override
    public void createAccount(final String name, String email, String password, String fullName, 
            String company, SuccessCallback<UserDTO> callback) {
        userManagementService.createSimpleUser(name, email, password, fullName, company,
                LocaleInfo.getCurrentLocale().getLocaleName(), emailConfirmationUrl,
                new AsyncCallbackImpl<UserDTO>(callback) {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            if (USER_ALREADY_EXISTS.equals(((UserManagementException) caught).getMessage())) {
                                view.setErrorMessage(i18n.userAlreadyExists(name));
                            }
                        } else {
                            view.setErrorMessage(i18n.errorCreatingUser(name, caught.getMessage()));
                        }
                    }
        });
    }
    
    @Override
    public void requestPasswordReset(final String username, String eMailAddress, SuccessCallback<Void> callback) {
        userManagementService.resetPassword(username, eMailAddress, passwordResetUrl, new AsyncCallbackImpl<Void>(callback) {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof UserManagementException) {
                    if (CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL.equals(caught.getMessage())) {
                        view.setErrorMessage(i18n.cannotResetPasswordWithoutValidatedEmail(username));
                    } else {
                        view.setErrorMessage(i18n.errorResettingPassword(username, caught.getMessage()));
                    }
                } else {
                    view.setErrorMessage(i18n.errorDuringPasswordReset(caught.getMessage()));
                }
            }
        });
    }
    
    @Override
    public void login(String username, String password, final SuccessCallback<SuccessInfo> callback) {
        userService.login(username, password, new AsyncCallback<SuccessInfo>() {
            @Override
            public void onSuccess(SuccessInfo result) {
                if (result.isSuccessful()) {
                    callback.onSuccess(result);
                } else {
                    if (SuccessInfo.FAILED_TO_LOGIN.equals(result.getMessage())) {
                        view.setErrorMessage(StringMessages.INSTANCE.failedToSignIn());
                    } else {
                        view.setErrorMessage(result.getMessage());
                    }
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                view.setErrorMessage(StringMessages.INSTANCE.failedToSignIn());
            }
        });
    }
    
    @Override
    public void logout() {
        userService.logout();
        eventBus.fireEvent(new AuthenticationRequestEvent(AuthenticationPlaces.SIGN_IN));
    }
    
    @Override
    public void refreshUserInfo() {
        userService.updateUser(true);
    }
    
    @Override
    public AuthenticationContext getAuthenticationContext() {
        return new AuthenticationContextImpl(userService.getCurrentUser());
    }
    
    @Override
    public void checkNewUserPopup(final Runnable hideUserHintCallback, Consumer<Runnable> showUserHintCallback) {
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                if (user != null) {
                    // No further user changes need to be handled
                    userService.removeUserStatusEventHandler(this);
                    hideUserHintCallback.run();
                } else {
                    if (!userService.wasUserRecentlyLoggedInOrDismissedTheHint()) {
                        showUserHintCallback.accept(userService::setUserLoginHintToStorage);
                    }
                }
            }
        }, true);
    }
    
    private abstract class AsyncCallbackImpl<T> implements AsyncCallback<T> {
        
        private final SuccessCallback<T> successCallback;
        
        private AsyncCallbackImpl(SuccessCallback<T> successCallback) {
            this.successCallback = successCallback;
        }
        
        @Override
        public final void onSuccess(T result) {
            this.successCallback.onSuccess(result);
        }
    }
    
    private class ErrorMessageViewImpl implements ErrorMessageView {
        @Override
        public void setErrorMessage(String errorMessage) {
            Window.alert(errorMessage);
        }
    }
}
