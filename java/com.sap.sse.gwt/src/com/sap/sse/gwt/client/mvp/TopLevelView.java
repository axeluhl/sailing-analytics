package com.sap.sse.gwt.client.mvp;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;

public interface TopLevelView {
    AcceptsOneWidget getContent();
    
    Widget asWidget();
    
    ErrorReporter getErrorReporter();
}
