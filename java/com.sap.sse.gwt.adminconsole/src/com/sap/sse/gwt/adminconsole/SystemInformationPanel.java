package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.ServerInfoRetriever;

public class SystemInformationPanel extends FlowPanel {

    private final Label buildVersionText;
    
    public SystemInformationPanel(final ServerInfoRetriever buildVersionRetriever, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        super();
        buildVersionText = new Label("");
        addFloatingWidget(buildVersionText);
        buildVersionRetriever.getServerInfo(new AsyncCallback<ServerInfoDTO>() {
            @Override
            public void onSuccess(ServerInfoDTO result) {
                buildVersionText.setText(stringMessages.version(result.getBuildVersion()));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                buildVersionText.setText(stringMessages.version(stringMessages.unknown()));
            }
        });
    }

    private void addFloatingWidget(Widget w) { 
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        add(w);
    }
}
