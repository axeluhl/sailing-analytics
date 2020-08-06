package com.sap.sse.gwt.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
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
                final int toIndex = fullVersionText.indexOf(" ") - 1;
                buildVersionText.setText(stringMessages.version(fullVersionText.substring(0, toIndex) + "..."));
            } else {
                buildVersionText.setText(stringMessages.version(fullVersionText));
            }
        } else {
            buildVersionText.setText(stringMessages.version(stringMessages.unknown()));
        }
        add(buildVersionText);
        if (buildVersionCut) {
            additionalInformation = new Anchor(stringMessages.additionalInformation());
            additionalInformation.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    AdditionalServerInformationDialog additionalInformationDialog = new AdditionalServerInformationDialog(
                            stringMessages, serverInfo);
                    additionalInformationDialog.show();
                }
            });
            add(additionalInformation);
        }
    }
}
