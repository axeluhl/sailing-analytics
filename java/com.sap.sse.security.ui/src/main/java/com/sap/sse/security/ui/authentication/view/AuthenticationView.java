package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public interface AuthenticationView extends AcceptsOneWidget, IsWidget {
    
    void setHeading(String heading);
    
}
