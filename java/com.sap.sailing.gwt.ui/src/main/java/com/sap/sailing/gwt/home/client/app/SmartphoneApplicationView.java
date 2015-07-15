package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.footer.Footer;
import com.sap.sailing.gwt.home.client.shared.header.Header;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link SmartphoneApplicationView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class SmartphoneApplicationView extends Composite implements ApplicationTopLevelView {
    interface ApplicationMobileViewUiBinder extends UiBinder<Widget, SmartphoneApplicationView> {
    }

    private static ApplicationMobileViewUiBinder uiBinder = GWT.create(ApplicationMobileViewUiBinder.class);

    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);
    
    @UiField(provided=true)
    Header headerPanel;

    @UiField
    Footer footerPanel;

    @UiField
    SimplePanel mainContentPanel;

    @UiField
    Element loadingMessage;

    public SmartphoneApplicationView(PlaceNavigator navigator) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }

    @Override
    public void showLoading(boolean visibile) {
        loadingMessage.getStyle().setVisibility(visibile ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}
