package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class ShareLinkDialog extends DataEntryDialog<String> {
    private final StringMessages stringMessages;
    private final PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings;
    private final LinkWithSettingsGenerator<Settings> linkWithSettingsGenerator;
    private FlowPanel flowPanel;
    private TextBox linkField;
    private CheckBox timeStampCheckbox;

    public ShareLinkDialog(String path, RaceboardContextDefinition raceboardContextDefinition,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings,
            RaceBoardPerspectiveOwnSettings currentRaceBoardPerspectiveOwnSettings, StringMessages stringMessages) {
        super("share the link", "url:", "ok", "cancel", null, null);
        GWT.log(perspectiveCompositeSettings.getSettingsPerComponentId().toString());
        this.perspectiveCompositeSettings = perspectiveCompositeSettings;
        GWT.log(currentRaceBoardPerspectiveOwnSettings.toString());
        GWT.log(perspectiveCompositeSettings.getSettingsPerComponentId().toString());
        this.linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(path, raceboardContextDefinition);
        this.stringMessages = stringMessages;
    }
    
    private void updateLink() {
        //TODO update RaceBoardPerspectiveOwnSettings to match the checkbox selections
        String url = this.linkWithSettingsGenerator.createUrl(perspectiveCompositeSettings);
        GWT.log(url);
        this.linkField.setText(url);
    }

    @Override
    protected String getResult() {
        return linkField.getText();
    }

    @Override
    protected Widget getAdditionalWidget() {
        linkField = createTextBox("");
        linkField.setReadOnly(true);
        timeStampCheckbox = createCheckbox("timestamp");
        timeStampCheckbox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        flowPanel = new FlowPanel();
        flowPanel.add(linkField);
        flowPanel.add(timeStampCheckbox);

        updateLink();
        return flowPanel;
    }
}
