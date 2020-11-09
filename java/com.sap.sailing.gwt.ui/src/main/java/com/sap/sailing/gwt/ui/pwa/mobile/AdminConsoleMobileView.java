package com.sap.sailing.gwt.ui.pwa.mobile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.pwa.mobile.footer.Footer;
import com.sap.sailing.gwt.ui.pwa.mobile.header.Header;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.mvp.TopLevelView;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link AdminConsoleMobileView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class AdminConsoleMobileView extends Composite implements TopLevelView {

    interface AdminConsoleMobileViewUiBinder extends UiBinder<Widget, AdminConsoleMobileView> {
    }

    private static AdminConsoleMobileViewUiBinder uiBinder = GWT.create(AdminConsoleMobileViewUiBinder.class);

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);

    @UiField
    Header header;

    @UiField
    Footer footer;

    @UiField
    SimplePanel content;

    @UiField
    Button settingsButton;

    public AdminConsoleMobileView(final EventBus eventBus) {
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        AdminConsoleMobileResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public AcceptsOneWidget getContent() {
        return content;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}