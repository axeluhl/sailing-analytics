package com.sap.sailing.gwt.home.shared.places.morelogininformation;

import java.util.Objects;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.ui.authentication.WithUserService;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;

/**
 * Abstract {@link Activity activity} implementation for {@link MoreLoginInformationView}s.
 */
public class AbstractMoreLoginInformationActivity extends AbstractActivity {

    private final WithUserService clientFactory;
    private final MoreLoginInformationView view;

    /**
     * Creates new {@link AbstractMoreLoginInformationActivity} instance for the provided view using the given factory.
     * 
     * @param clientFactory
     *            {@link WithUserService client factory} providing access to the {@link UserService}
     * @param view
     *            the {@link MoreLoginInformationView} to show
     */
    protected AbstractMoreLoginInformationActivity(WithUserService clientFactory, MoreLoginInformationView view) {
        this.clientFactory = clientFactory;
        this.view = view;
    }

    @Override
    public final void start(AcceptsOneWidget panel, EventBus eventBus) {
        final UserStatusEventHandler handler = (user, preAuthenticated) -> view.setRegisterControlVisible(Objects.isNull(user));
        this.clientFactory.getUserService().addUserStatusEventHandler(handler, true);
        panel.setWidget(view);
    }

}
