package com.sap.sse.gwt.client.mvp.example.goodbye;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View interface. Extends IsWidget so a view impl can easily provide its container widget.
 * 
 * @author drfibonacci
 */
public interface GoodbyeView extends IsWidget {
    void setName(String helloName);
}