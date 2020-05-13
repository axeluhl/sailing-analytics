package com.sap.sse.gwt.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.ServerInfoDTO;

public class SystemInformationPanel extends FlowPanel {

    private final Label buildVersionText;
    private String fullVersionText;
    private boolean buildVersionCut;
    private Anchor additionalInformation;

    public SystemInformationPanel(final ServerInfoDTO serverInfo, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        super();
        buildVersionText = new Label();
        if (serverInfo != null) {
            fullVersionText = serverInfo.getBuildVersion();
            if (fullVersionText.length() > 70) {
                buildVersionCut = true;
                buildVersionText.setText(stringMessages.version(fullVersionText.substring(0, 100) + "..."));
            } else {
                buildVersionText.setText(stringMessages.version(fullVersionText));
            }
        } else {
            buildVersionText.setText(stringMessages.version(stringMessages.unknown()));
        }
        addFloatingWidget(buildVersionText);
        if(buildVersionCut) {
            additionalInformation = new Anchor(stringMessages.additionalInformation());
            additionalInformation.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    AdditionalInformationDialog additionalInformationDialog = new AdditionalInformationDialog(
                            stringMessages.additionalInformation(), stringMessages.version(fullVersionText), stringMessages.ok(),
                            stringMessages.cancel(), /* validator */ null, /* animationEnabled */ false,
                            /* callback */null);
                    additionalInformationDialog.show();
                }
            });
            addFloatingWidget(additionalInformation);
        }
    }

    private void addFloatingWidget(Widget w) {
        w.getElement().getStyle().setFloat(Style.Float.LEFT);
        w.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        add(w);
    }
}
