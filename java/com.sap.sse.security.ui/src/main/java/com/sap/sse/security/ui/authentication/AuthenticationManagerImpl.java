package com.sap.sse.security.ui.authentication;

import static com.sap.sse.security.shared.UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL;
import static com.sap.sse.security.shared.UserManagementException.USER_ALREADY_EXISTS;

import java.util.function.Consumer;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
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
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                eventBus.fireEvent(new AuthenticationContextEvent(new AuthenticationContextImpl(user, userService)));
            }
        });
        eventBus.addHandler(AuthenticationSignOutRequestEvent.TYPE, new AuthenticationSignOutRequestEvent.Handler() {
            @Override
            public void onUserManagementSignOutRequestEvent(AuthenticationSignOutRequestEvent event) {
                    logout();
            }
        });
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            @SuppressWarnings("unused")
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                final String localeParam = Window.Location.getParameter(LocaleInfo.getLocaleQueryParam());
                // If a user is already authenticated while opening the page, we only trigger a reload if no locale is given by the URL
                if (ExperimentalFeatures.REFRESH_ON_LOCALE_CHANGE_IN_USER_PROFILE && preAuthenticated
                        && (localeParam == null || localeParam.isEmpty())) {
                    redirectWithLocaleForAuthenticatedUser();
                }
            }
        }, true);
    }

    @Override
    public void createAccount(final String name, String email, String password, String fullName, 
            String company, SuccessCallback<UserDTO> callback) {
        userManagementService.createSimpleUser(name, email, password, fullName, company,
                LocaleInfo.getCurrentLocale().getLocaleName(), emailConfirmationUrl, "tenant",
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
                    if (ExperimentalFeatures.REFRESH_ON_LOCALE_CHANGE_IN_USER_PROFILE) {
                        // when a user logs in we explicitly switch to the user's locale event if a locale is given by the URL
                        redirectIfLocaleIsSetAndLocaleIsNotGivenInTheURL(result.getUserDTO().getA().getLocale());
                    }
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
    
    /**
     * Refresh information of current user.
     */
    private void refreshUserInfo() {
        userService.updateUser(true);
    }
    
    @Override
    public void updateUserProperties(String fullName, String company, String localeName, final AsyncCallback<UserDTO> callback) {
        final UserDTO currentUser = getAuthenticationContext().getCurrentUser();
        final String username = currentUser.getName();
        final String locale = currentUser.getLocale();
        userManagementService.updateUserProperties(username, fullName, company, localeName, new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(UserDTO result) {
                refreshUserInfo();
                callback.onSuccess(result);
                if(!Util.equalsWithNull(locale, localeName)) {
                    redirectIfLocaleIsSetAndLocaleIsNotGivenInTheURL(localeName);
                }
            }
        });
    }

    /**
     * Switches to the locale to the current user's locale when a user logs in.
     */
    private void redirectWithLocaleForAuthenticatedUser() {
        final AuthenticationContext authenticationContext = getAuthenticationContext();
        if(authenticationContext.isLoggedIn()) {
            redirectIfLocaleIsSetAndLocaleIsNotGivenInTheURL(authenticationContext.getCurrentUser().getLocale());
        }
    }

    private void redirectIfLocaleIsSetAndLocaleIsNotGivenInTheURL(String locale) {
        if(shouldChangeLocale(locale)) {
            Window.Location.reload();
        }
    }

    private boolean shouldChangeLocale(String locale) {
        if(locale == null || locale.isEmpty()) {
            // If the user currently has no locale preference, we do not refresh
            return false;
        }
        final String localeParam = Window.Location.getParameter(LocaleInfo.getLocaleQueryParam());
        if (localeParam != null && !localeParam.isEmpty()) {
            // If the locale is specified in the URL, we do not refresh
            return false;
        }
        final String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        // If the current locale is equal to the preferred language, a refresh isn't necessary
        return !currentLocale.equals(locale);
    }

    @Override
    public AuthenticationContext getAuthenticationContext() {
        return new AuthenticationContextImpl(userService.getCurrentUser(), userService);
    }
    
    @Override
    public void checkNewUserPopup(final Runnable hideUserHintCallback, Consumer<Runnable> showUserHintCallback) {
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
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
            Notification.notify(errorMessage, NotificationType.ERROR);
        }
    }
}
