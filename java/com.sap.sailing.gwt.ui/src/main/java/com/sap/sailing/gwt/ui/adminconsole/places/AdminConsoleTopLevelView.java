package com.sap.sailing.gwt.ui.adminconsole.places;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.mvp.TopLevelView;

/**
 * This is the top-level view of the application. Every time another presenter wants to reveal itself,
 * {@link AdminConsoleTopLevelView} will add its content of the target inside the {@code mainContantPanel}.
 */
public class AdminConsoleTopLevelView extends Composite implements TopLevelView { 
   
    private static ErrorReporter errorReporter = new DefaultErrorReporter<StringMessages>(StringMessages.INSTANCE);

    SimplePanel mainContentPanel;

    public AdminConsoleTopLevelView(EventBus eventBus) {
        mainContentPanel = new SimplePanel();
    }

    @Override
    public AcceptsOneWidget getContent() {
        return mainContentPanel;
    }
    
    @Override
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }
    
}
