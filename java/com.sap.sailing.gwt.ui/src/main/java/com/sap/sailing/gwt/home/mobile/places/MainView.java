package com.sap.sailing.gwt.home.mobile.places;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.partials.footer.Footer;
import com.sap.sailing.gwt.home.mobile.partials.header.Header;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link MainView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class MainView extends Composite {
    interface MyBinder extends UiBinder<Widget, MainView> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);

    @UiField(provided=true)
    Header headerPanel;

    @UiField(provided=true)
    Footer footerPanel;

    @UiField
    SimplePanel mainContentPanel;

    public MainView(MobileApplicationClientFactory appContext, EventBus eventBus) {
        headerPanel = new Header(appContext);
        footerPanel = new Footer(appContext);
        initWidget(uiBinder.createAndBindUi(this));
    }


    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }
    
    

    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}
