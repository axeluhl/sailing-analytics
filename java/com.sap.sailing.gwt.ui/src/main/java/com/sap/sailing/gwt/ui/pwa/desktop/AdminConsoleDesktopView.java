package com.sap.sailing.gwt.ui.pwa.desktop;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.pwa.desktop.footer.Footer;
import com.sap.sailing.gwt.ui.pwa.desktop.header.Header;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.security.ui.client.UserService;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link AdminConsoleDesktopView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class AdminConsoleDesktopView extends Composite implements TopLevelView {
    
    interface AdminConsoleDesktopViewUiBinder extends UiBinder<Widget, AdminConsoleDesktopView> {
    }

    private static AdminConsoleDesktopViewUiBinder uiBinder = GWT.create(AdminConsoleDesktopViewUiBinder.class);

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);
    
    @UiField
    Header header;
    
    @UiField
    Footer footer;

    @UiField
    SimplePanel content;

    public AdminConsoleDesktopView(final EventBus eventBus, final UserService userService, final SailingServiceWriteAsync sailingService) {
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
