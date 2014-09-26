package com.sap.sailing.gwt.ui.client.shared.panels;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public class SystemInformationPanel extends FlowPanel {

    private final Label buildVersionText;
    
    public SystemInformationPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter) {
        super();
        
        buildVersionText = new Label("");
        addFloatingWidget(buildVersionText);
        
        sailingService.getBuildVersion(new AsyncCallback<String>() {
            
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
