package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;
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
    private VerticalPanel verticalPanel;
    private CheckBox timeStampCheckbox;
    private CheckBox windChartCheckBox;
    private CheckBox leaderBoardPanelCheckBox;
    private CheckBox competitorChartCheckBox;
    private CheckBox filterSetNameCheckBox;
    private CheckBox competitorSelectionCheckBox;
    private Anchor linkAnchor;

    public ShareLinkDialog(String path, RaceboardContextDefinition raceboardContextDefinition,
            PerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings,
            StringMessages stringMessages) {
        super(stringMessages.shareTheLink(), "", "ok", stringMessages.cancel(), null, null);
        this.lifecycle = lifecycle;
        this.perspectiveCompositeSettings = perspectiveCompositeSettings;
        this.linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(path, raceboardContextDefinition);
        this.stringMessages = stringMessages;
    }
    
    private String assembleLink() {
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchedSettings = patchSettings();
        String url = this.linkWithSettingsGenerator.createUrl(patchedSettings);
        return url;
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
        return assembleLink();
    }

    @Override
    protected Widget getAdditionalWidget() {
        timeStampCheckbox = createCheckbox(stringMessages.timeStampCheckBoxLabel());
        timeStampCheckbox.setValue(true);
        timeStampCheckbox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                linkAnchor.setHref(assembleLink());
            }
        });
        windChartCheckBox = createCheckbox(stringMessages.windChartCheckBoxLabel());
        windChartCheckBox.setValue(true);
        windChartCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                linkAnchor.setHref(assembleLink());
            }
        });
        leaderBoardPanelCheckBox = createCheckbox(stringMessages.leaderBoardCheckBoxLabel());
        leaderBoardPanelCheckBox.setValue(true);
        leaderBoardPanelCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                linkAnchor.setHref(assembleLink());
            }
        });
        competitorChartCheckBox = createCheckbox(stringMessages.competitorChartCheckBoxLabel());
        competitorChartCheckBox.setValue(true);
        competitorChartCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                linkAnchor.setHref(assembleLink());
            }
        });
        filterSetNameCheckBox = createCheckbox(stringMessages.filterSetNameCheckBoxLabel());
        filterSetNameCheckBox.setValue(true);
        filterSetNameCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                linkAnchor.setHref(assembleLink());
            }
        });
        competitorSelectionCheckBox = createCheckbox(stringMessages.competitorSelectionCheckBoxLabel());
        competitorSelectionCheckBox.setValue(true);
        competitorSelectionCheckBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                linkAnchor.setHref(assembleLink());
            }
        });
        linkAnchor = new Anchor(stringMessages.linkSharingAnchorText(), assembleLink());
        verticalPanel = new VerticalPanel();
        verticalPanel.add(timeStampCheckbox);
        verticalPanel.add(windChartCheckBox);
        verticalPanel.add(leaderBoardPanelCheckBox);
        verticalPanel.add(competitorChartCheckBox);
        verticalPanel.add(filterSetNameCheckBox);
        verticalPanel.add(competitorSelectionCheckBox);
        verticalPanel.add(linkAnchor);
        return verticalPanel;
    }
}
