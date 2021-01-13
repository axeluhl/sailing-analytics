package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.NavigatorUtil;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
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
    private final SailingServiceAsync sailingService;
    private final boolean isLargeScreen;
    private CheckBox timeStampCheckbox;
    private CheckBox windChartCheckBox;
    private CheckBox leaderBoardPanelCheckBox;
    private CheckBox competitorChartCheckBox;
    private CheckBox filterSetNameCheckBox;
    private CheckBox competitorSelectionCheckBox;
    private CheckBox tagsCheckBox;
    private CheckBox maneuverCheckBox;
    private CheckBox zoomCheckBox;
    private TextBox linkField;
    private Image qrCodeImage;

    public ShareLinkDialog(String path, RaceboardContextDefinition raceboardContextDefinition,
            PerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings,
            SailingServiceAsync sailingService, boolean isLargeScreen, StringMessages stringMessages) {
        super(stringMessages.shareTheLink(), "", stringMessages.ok(), stringMessages.cancel(), /* validator */ null,
                /* callback */ null);
        this.lifecycle = lifecycle;
        this.perspectiveCompositeSettings = perspectiveCompositeSettings;
        this.sailingService = sailingService;
        RaceboardContextDefinition newRaceBoardContextDefinition = new RaceboardContextDefinition(
                raceboardContextDefinition.getRegattaName(), raceboardContextDefinition.getRaceName(),
                raceboardContextDefinition.getLeaderboardName(), raceboardContextDefinition.getLeaderboardGroupName(),
                raceboardContextDefinition.getLeaderboardGroupId(), raceboardContextDefinition.getEventId(), null);
        this.linkWithSettingsGenerator = new LinkWithSettingsGenerator<>(path, newRaceBoardContextDefinition);
        this.isLargeScreen = isLargeScreen;
        this.stringMessages = stringMessages;
    }

    void updateLink() {
        String url = assembleLink();
        linkField.setText(url);
        if(isLargeScreen) {
            createQrCode(url);
        }
    }

    private void createQrCode(String url) {
        sailingService.createRaceBoardLinkQrCode(url, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Qrcode generation failed: ", caught);
            }
            @Override
            public void onSuccess(String result) {
                GWT.log("Qrcode generated for url: " + url);
                qrCodeImage.setUrl("data:image/png;base64, " + result);
            }
        });
    }

    private String assembleLink() {
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchedSettings = patchSettings();
        String url = this.linkWithSettingsGenerator.createUrl(patchedSettings);
        return url;
    }

    private PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchSettings() {
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> patchedSettings = lifecycle
                .createDefaultSettings();
        SettingsUtil.copyValues(perspectiveCompositeSettings, patchedSettings);
        final RaceBoardPerspectiveOwnSettings patchedPerspectiveOwnSettings = patchedSettings
                .getPerspectiveOwnSettings();
        if(isLargeScreen) {
            if (!competitorChartCheckBox.getValue()) {
                patchedPerspectiveOwnSettings.resetShowCompetitorsChart();
            }
            if (!leaderBoardPanelCheckBox.getValue()) {
                patchedPerspectiveOwnSettings.resetShowLeaderBoard();
            }
            if (!windChartCheckBox.getValue()) {
                patchedPerspectiveOwnSettings.resetShowWindChart();
            }
            if (!tagsCheckBox.getValue()) {
                patchedPerspectiveOwnSettings.resetShowTags();
            }
            if (!maneuverCheckBox.getValue()) {
                patchedPerspectiveOwnSettings.resetShowManeuver();
            }
            if (!zoomCheckBox.getValue()) {
                patchedPerspectiveOwnSettings.resetZoomStart();
                patchedPerspectiveOwnSettings.resetZoomEnd();
            }
        }
        if (!timeStampCheckbox.getValue()) {
            patchedPerspectiveOwnSettings.resetInitialDurationAfterRaceStartInReplay();
        }
        if (!filterSetNameCheckBox.getValue()) {
            patchedPerspectiveOwnSettings.resetActiveCompetitorsFilterSetName();
        }
        if (!competitorSelectionCheckBox.getValue()) {
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
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(30);
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        VerticalPanel settingsPanel = new VerticalPanel();
        settingsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.add(settingsPanel);
        timeStampCheckbox = createCheckbox(stringMessages.timeStampCheckBoxLabel());
        timeStampCheckbox.setValue(true);
        timeStampCheckbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        settingsPanel.add(timeStampCheckbox);
        if (isLargeScreen) {
            leaderBoardPanelCheckBox = createCheckbox(stringMessages.leaderboardCheckBoxLabel());
            leaderBoardPanelCheckBox.setValue(true);
            leaderBoardPanelCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateLink();
                }
            });
            settingsPanel.add(leaderBoardPanelCheckBox);
            tagsCheckBox = createCheckbox(stringMessages.tagsCheckBoxLabel());
            tagsCheckBox.setValue(true);
            tagsCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateLink();
                }
            });
            settingsPanel.add(tagsCheckBox);
        }
        filterSetNameCheckBox = createCheckbox(stringMessages.filterSetNameCheckBoxLabel());
        filterSetNameCheckBox.setValue(true);
        filterSetNameCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        settingsPanel.add(filterSetNameCheckBox);
        competitorSelectionCheckBox = createCheckbox(stringMessages.competitorSelectionCheckBoxLabel());
        competitorSelectionCheckBox.setValue(true);
        competitorSelectionCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        settingsPanel.add(competitorSelectionCheckBox);
        if (isLargeScreen) {
            windChartCheckBox = createCheckbox(stringMessages.windChartCheckBoxLabel());
            windChartCheckBox.setValue(true);
            windChartCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateLink();
                }
            });
            settingsPanel.add(windChartCheckBox);
            competitorChartCheckBox = createCheckbox(stringMessages.competitorChartCheckBoxLabel());
            competitorChartCheckBox.setValue(true);
            competitorChartCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateLink();
                }
            });
            settingsPanel.add(competitorChartCheckBox);
            maneuverCheckBox = createCheckbox(stringMessages.maneuverCheckBoxLabel());
            maneuverCheckBox.setValue(true);
            maneuverCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateLink();
                }
            });
            settingsPanel.add(maneuverCheckBox);
            zoomCheckBox = createCheckbox(stringMessages.zoomCheckBoxLabel());
            zoomCheckBox.setValue(true);
            zoomCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateLink();
                }
            });
            settingsPanel.add(zoomCheckBox);
        }
        VerticalPanel linkContentPanel = new VerticalPanel();
        linkContentPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        linkField = createTextBox(assembleLink());
        linkContentPanel.add(linkField);
        if(isLargeScreen) {
            qrCodeImage = new Image();
            qrCodeImage.ensureDebugId("regattaSharingQrCode");
            qrCodeImage.setPixelSize(400, 400);
            if(NavigatorUtil.clientHasNavigatorCopyToClipboardSupport()) {
                Anchor copyToClipBoardAnchor = new Anchor(stringMessages.copyToClipboard());
                copyToClipBoardAnchor.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        NavigatorUtil.copyToClipboard(linkField.getText());
                    }
                });
                linkContentPanel.add(copyToClipBoardAnchor);
            }
            linkContentPanel.add(qrCodeImage);
        }
        mainPanel.add(linkContentPanel);
        updateLink();
        return mainPanel;
    }
}
