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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;

public class ShareLinkDialog extends DataEntryDialog<String> {
    private final PerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> lifecycle;
    private final PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings;
    private final LinkWithSettingsGenerator<Settings> linkWithSettingsGenerator;
    private final SailingServiceAsync sailingService;
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
    private VerticalPanel mainPanel;

    public ShareLinkDialog(String path, PerspectiveLifecycle<RaceBoardPerspectiveOwnSettings> lifecycle,
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> perspectiveCompositeSettings,
            SailingServiceAsync sailingService, StringMessages stringMessages,
            LinkWithSettingsGenerator<Settings> linkWithSettingsGenerator) {
        super(stringMessages.shareTheLink(), "", stringMessages.ok(), stringMessages.cancel(), /* validator */ null,
                /* callback */ null);
        this.lifecycle = lifecycle;
        this.perspectiveCompositeSettings = perspectiveCompositeSettings;
        this.sailingService = sailingService;
        this.linkWithSettingsGenerator = linkWithSettingsGenerator;
        mainPanel = new VerticalPanel();
        mainPanel.setSpacing(30);
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        final VerticalPanel settingsPanel = new VerticalPanel();
        settingsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        mainPanel.add(settingsPanel);
        initializeCheckboxes(stringMessages, settingsPanel);
        VerticalPanel linkContentPanel = new VerticalPanel();
        linkContentPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        linkField = createTextBox(assembleLink());
        linkContentPanel.add(linkField);
        qrCodeImage = new Image();
        qrCodeImage.ensureDebugId("regattaSharingQrCode");
        qrCodeImage.setPixelSize(400, 400);
        qrCodeImage.setAltText(stringMessages.alternateTextIfQRCodeTooBig());
        if (NavigatorUtil.clientHasNavigatorCopyToClipboardSupport()) {
            Anchor copyToClipBoardAnchor = new Anchor(stringMessages.copyToClipboard());
            copyToClipBoardAnchor.addClickHandler(event -> NavigatorUtil.copyToClipboard(linkField.getText()));
            linkContentPanel.add(copyToClipBoardAnchor);
        }
        linkContentPanel.add(qrCodeImage);
        mainPanel.add(linkContentPanel);
    }

    private void initializeCheckboxes(StringMessages stringMessages, final VerticalPanel settingsPanel) {
        timeStampCheckbox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.timeStampCheckBoxLabel());
        leaderBoardPanelCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.leaderboardCheckBoxLabel());
        tagsCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.tagsCheckBoxLabel());
        filterSetNameCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.filterSetNameCheckBoxLabel());
        competitorSelectionCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.competitorSelectionCheckBoxLabel());
        windChartCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.windChartCheckBoxLabel());
        competitorChartCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.competitorChartCheckBoxLabel());
        maneuverCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.maneuverCheckBoxLabel());
        zoomCheckBox = createCheckBoxAndAddToPanel(settingsPanel, stringMessages.zoomCheckBoxLabel());
    }
    
    public void initLinkAndShow() {
        String url = linkWithSettingsGenerator.createUrl(perspectiveCompositeSettings);
        createQrCode(url, this::show);
    }

    void updateLink() {
        String url = assembleLink();
        linkField.setText(url);
        createQrCode(url, () -> {});
    }

    private void createQrCode(String url, Runnable callback) {
        sailingService.createRaceBoardLinkQrCode(url, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Qrcode generation failed: ", caught);
                callback.run();
            }
            @Override
            public void onSuccess(String result) {
                if(result == null){
                    GWT.log("Qrcode generation failed.");
                }else {
                    GWT.log("Qrcode generated for url: " + url);
                }
                qrCodeImage.setUrl("data:image/png;base64, " + result);
                callback.run();
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
        return mainPanel;
    }
    
    private CheckBox createCheckBoxAndAddToPanel(VerticalPanel settingsPanel, String label) {
        CheckBox checkBox = createCheckbox(label);
        checkBox.setValue(true);
        checkBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateLink();
            }
        });
        settingsPanel.add(checkBox);
        return checkBox;
    }
}
