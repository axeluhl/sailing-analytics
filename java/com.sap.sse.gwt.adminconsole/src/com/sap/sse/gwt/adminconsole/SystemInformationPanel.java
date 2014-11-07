package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;

public class SystemInformationPanel extends FlowPanel {

    private final Label buildVersionText;
    
    public SystemInformationPanel(final BuildVersionRetriever buildVersionRetriever, final ErrorReporter errorReporter) {
        super();
        buildVersionText = new Label("");
        addFloatingWidget(buildVersionText);
        buildVersionRetriever.getBuildVersion(new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                buildVersionText.setText("Version: " + result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                buildVersionText.setText("Version: " + "Unknown");
            }
        });
    }

    private void addFloatingWidget(Widget w) { 
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        add(w);
    }
    
}
