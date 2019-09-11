package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;

public class SystemInformationPanel extends FlowPanel {

    private final Label buildVersionText;
    
    public SystemInformationPanel(final ServerInfoDTO serverInfo, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        super();
        buildVersionText = new Label("");
        addFloatingWidget(buildVersionText);
        if (serverInfo != null) {
            buildVersionText.setText(stringMessages.version(serverInfo.getBuildVersion()));
        } else {
            buildVersionText.setText(stringMessages.version(stringMessages.unknown()));
        }
    }

    private void addFloatingWidget(Widget w) { 
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        add(w);
    }
}
