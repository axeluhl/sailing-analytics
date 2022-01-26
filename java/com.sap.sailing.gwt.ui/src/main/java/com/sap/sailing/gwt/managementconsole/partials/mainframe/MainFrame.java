package com.sap.sailing.gwt.managementconsole.partials.mainframe;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.partials.authentication.decorator.AuthorizedDecorator;
import com.sap.sailing.gwt.managementconsole.partials.header.Header;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

/**
 * The management console application's main frame containing the navigation {@link Header header} and the
 * {@link AcceptsOneWidget content container} which is wrapped into an {@link AuthorizedDecorator content decorator}.
 */
public class MainFrame extends ResizeComposite implements NeedsAuthenticationContext {

    interface MainFrameUiBinder extends UiBinder<Widget, MainFrame> {
    }

    private static MainFrameUiBinder uiBinder = GWT.create(MainFrameUiBinder.class);

    @UiField
    MainFrameResources local_res;

    @UiField(provided = true)
    AuthorizedDecorator decorator;

    @UiField
    Header header;

    @UiField
    AcceptsOneWidget contentContainer;

    public MainFrame(final NotLoggedInPresenter presenter) {
        this.decorator = new AuthorizedDecorator(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    @Override
    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        decorator.setAuthenticationContext(authenticationContext);
        header.setAuthenticationContext(authenticationContext);
    }

    public Header getHeader() {
        return header;
    }

    public AcceptsOneWidget getContentContainer() {
        return contentContainer;
    }
}
