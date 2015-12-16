package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import static com.sap.sse.security.shared.UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL;

import java.util.HashMap;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordRecoveryActivity<CF extends ClientFactoryWithUserManagementService & ClientFactory> extends AbstractActivity implements PasswordRecoveryView.Presenter {

    private final CF clientFactory;
    private final PlaceController placeController;
    private final PasswordRecoveryPlace place;
    private final PasswordRecoveryView view = new PasswordRecoveryViewImpl();

    public PasswordRecoveryActivity(PasswordRecoveryPlace place, CF clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.place = place;
    }
    
    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
    }

    @Override
    public void resetPassword(String email, final String username) {
        clientFactory.getUserManagement().resetPassword(username, email,
                EntryPointLinkFactory.createPasswordResetLink(new HashMap<String, String>()), 
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        clientFactory.getEventBus().fireEvent(new UserManagementRequestEvent());
                        placeController.goTo(place.getNextTarget());
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        StringMessages i18n = StringMessages.INSTANCE;
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
}
