package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.shared.footer.Footer;
import com.sap.sailing.gwt.home.client.shared.header.Header;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link TabletAndDesktopApplicationView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class TabletAndDesktopApplicationView extends Composite implements ApplicationTopLevelView {
    interface TabletAndDesktopApplicationViewUiBinder extends UiBinder<Widget, TabletAndDesktopApplicationView> {
    }

    private static TabletAndDesktopApplicationViewUiBinder uiBinder = GWT.create(TabletAndDesktopApplicationViewUiBinder.class);

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);

    @UiField(provided=true)
    Header headerPanel;

    @UiField(provided=true)
    Footer footerPanel;

    @UiField
    SimplePanel mainContentPanel;

    public TabletAndDesktopApplicationView(HomePlacesNavigator navigator, EventBus eventBus) {
        headerPanel = new Header(navigator, eventBus);
        footerPanel = new Footer(navigator);
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }
    
    @Override
    public void showLoading(boolean visible) {
    }
    
    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}
