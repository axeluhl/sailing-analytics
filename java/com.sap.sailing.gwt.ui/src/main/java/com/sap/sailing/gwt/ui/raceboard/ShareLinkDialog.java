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
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;

public class ShareLinkDialog extends DataEntryDialog<String> {
    private final StringMessages stringMessages;
    private final PerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> lifecycle;
    private final PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings;
    private final LinkWithSettingsGenerator<Settings> linkWithSettingsGenerator;
    private FlowPanel flowPanel;
    private TextBox linkField;
    private CheckBox timeStampCheckbox;
    private CheckBox windChartCheckBox;
    private CheckBox leaderBoardPanelCheckBox;
    private CheckBox competitorChartCheckBox;
    private CheckBox filterSetNameCheckBox;
    private CheckBox competitorSelectionCheckBox;

    public ShareLinkDialog(String path, RaceboardContextDefinition raceboardContextDefinition,
            PerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings,
            StringMessages stringMessages) {
        super("share the link", "url:", "ok", "cancel", null, null);
        this.lifecycle = lifecycle;
        this.perspectiveCompositeSettings = perspectiveCompositeSettings;
        this.linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(path, raceboardContextDefinition);
        this.stringMessages = stringMessages;
    }
    
    private void updateLink() {
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchedSettings = patchSettings();
        String url = this.linkWithSettingsGenerator.createUrl(patchedSettings);
        this.linkField.setText(url);
    }

    private PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchSettings() {
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchedSettings = lifecycle.createDefaultSettings();
        SettingsUtil.copyValues(perspectiveCompositeSettings, patchedSettings);
        final RaceBoardPerspectiveOwnSettings patchedPerspectiveOwnSettings = patchedSettings.getPerspectiveOwnSettings();
        if(!timeStampCheckbox.getValue()) {
            patchedPerspectiveOwnSettings.resetInitialDurationAfterRaceStartInReplay();
        }
        if(!competitorChartCheckBox.getValue()) {
            patchedPerspectiveOwnSettings.resetShowCompetitorsChart();
        }
        if(!leaderBoardPanelCheckBox.getValue()) {
            patchedPerspectiveOwnSettings.resetShowLeaderBoard();
        }
        if(!windChartCheckBox.getValue()) {
            patchedPerspectiveOwnSettings.resetShowWindChart();
        }
        if(!filterSetNameCheckBox.getValue()) {
            patchedPerspectiveOwnSettings.resetActiveCompetitorsFilterSetName();;
        }
        
        if(!competitorSelectionCheckBox.getValue()) {
            patchedPerspectiveOwnSettings.resetSelectedCompetitor();
            patchedPerspectiveOwnSettings.resetSelectedCompetitors();
        }
        return patchedSettings;
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
        windChartCheckBox = createCheckbox("windChartCheckBox");
        windChartCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        leaderBoardPanelCheckBox = createCheckbox("leaderBoardPanelCheckBox");
        leaderBoardPanelCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        competitorChartCheckBox = createCheckbox("competitorChartCheckBox");
        competitorChartCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        filterSetNameCheckBox = createCheckbox("filterSetNameCheckBox");
        filterSetNameCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        competitorSelectionCheckBox = createCheckbox("competitorSelectionCheckBox");
        competitorSelectionCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        
        flowPanel = new FlowPanel();
        flowPanel.add(linkField);
        flowPanel.add(timeStampCheckbox);
        flowPanel.add(windChartCheckBox);
        flowPanel.add(leaderBoardPanelCheckBox);
        flowPanel.add(competitorChartCheckBox);
        flowPanel.add(filterSetNameCheckBox);
        flowPanel.add(competitorSelectionCheckBox);

        updateLink();
        return flowPanel;
    }
}
