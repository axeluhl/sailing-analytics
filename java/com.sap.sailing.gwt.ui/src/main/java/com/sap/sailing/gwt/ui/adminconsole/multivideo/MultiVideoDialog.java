package com.sap.sailing.gwt.ui.adminconsole.multivideo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.JSDownloadUtils;
import com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback;
import com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSHrefCallback;
import com.sap.sailing.gwt.ui.client.media.TimeFormatUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MimeType;

public class MultiVideoDialog extends DialogBox {
    private static final StyleHolder style = GWT.create(StyleHolder.class);
    private StringMessages stringMessages;
    private List<RemoteFileInfo> remoteFiles = new ArrayList<>();
    private FlexTable dataTable;
    private MediaServiceAsync mediaService;
    private Button doScanButton;
    private Label statusLabel;
    private SailingServiceAsync sailingService;
    private boolean isWorking;
    private Button doSaveButton;

    public MultiVideoDialog(SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.mediaService = mediaService;
        this.sailingService = sailingService;
        setGlassEnabled(true);

        FlowPanel mainContent = new FlowPanel();

        Label indexUrl = new Label(stringMessages.multiVideoURLOfIndex());
        mainContent.add(indexUrl);
        ;
        TextBox urlInput = new TextBox();
        mainContent.add(urlInput);
        doScanButton = new Button(stringMessages.multiVideoScan());
        mainContent.add(doScanButton);
        doScanButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setWorking(true);
                retrieveRemoteFileList(urlInput.getText());
            }
        });

        statusLabel = new Label("i18n idle");
        mainContent.add(statusLabel);

        FlowPanel buttonPanel = new FlowPanel();

        DockPanel dockPanel = new DockPanel();
        add(dockPanel);
        dockPanel.add(new ScrollPanel(mainContent), DockPanel.CENTER);

        dataTable = new FlexTable();
        style.style().ensureInjected();
        dataTable.addStyleName(style.style().tableStyle());
        mainContent.add(dataTable);

        Button cancelButton = new Button(stringMessages.close());
        cancelButton.getElement().getStyle().setMargin(3, Unit.PX);
        cancelButton.ensureDebugId("CancelButton");
        buttonPanel.add(cancelButton);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MultiVideoDialog.this.hide();
            }
        });

        doSaveButton = new Button("i18n save");
        buttonPanel.add(doSaveButton);
        doSaveButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setWorking(true);
                startNextLinkingRemoteTask();
            }
        });
        doSaveButton.setEnabled(false);

        dockPanel.add(buttonPanel, DockPanel.SOUTH);

        updateUI();
    }

    public void setWorking(boolean working) {
        this.isWorking = working;
        updateUI();
    }

    protected void updateUI() {
        int y = 0;
        dataTable.clear();
        dataTable.setWidget(y, 0, new Label("i18n url"));
        dataTable.setWidget(y, 1, new Label("i18n status"));
        dataTable.setWidget(y, 2, new Label(stringMessages.duration()));
        dataTable.setWidget(y, 3, new Label(stringMessages.startTime()));
        dataTable.setWidget(y, 4, new Label(stringMessages.mimeType()));
        dataTable.setWidget(y, 5, new Label(stringMessages.linkedRaces()));
        y++;
        for (RemoteFileInfo remoteFile : remoteFiles) {
            Anchor link = new Anchor(remoteFile.url);
            link.setHref(remoteFile.url);
            link.setTarget("_blank");
            dataTable.setWidget(y, 0, link);
            dataTable.setWidget(y, 1, new Label(asString(remoteFile.status)));
            if (remoteFile.duration == null) {
                dataTable.setWidget(y, 2, new Label("-"));
            } else {
                TextBox durationInput = new TextBox();
                durationInput.setText(TimeFormatUtil.durationToHrsMinSec(remoteFile.duration));
                durationInput.addValueChangeHandler(new ValueChangeHandler<String>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        remoteFile.duration = TimeFormatUtil.hrsMinSecToMilliSeconds(event.getValue());
                        updateUI();
                    }
                });
                dataTable.setWidget(y, 2, durationInput);
            }
            if (remoteFile.startTime == null) {
                dataTable.setWidget(y, 3, new Label("-"));
            } else {
                dataTable.setWidget(y, 3,
                        new Label(TimeFormatUtil.DATETIME_FORMAT.format(remoteFile.startTime.asDate())));
            }
            if (remoteFile.mime == null) {
                if (remoteFile.message == null) {
                    dataTable.setWidget(y, 4, new Label("-"));
                } else {
                    dataTable.setWidget(y, 4, new Label(remoteFile.message));
                }
            } else {
                ListBox mimeTypeBox = new ListBox();
                mimeTypeBox.setMultipleSelect(false);
                mimeTypeBox.addItem(MimeType.mp4.name());
                mimeTypeBox.addItem(MimeType.mp4panorama.name());
                mimeTypeBox.addChangeHandler(new ChangeHandler() {

                    @Override
                    public void onChange(ChangeEvent event) {
                        remoteFile.mime = MimeType.valueOf(mimeTypeBox.getSelectedValue());
                        updateUI();
                    }
                });
                mimeTypeBox.setSelectedIndex(MimeType.mp4 == remoteFile.mime ? 0 : 1);
                dataTable.setWidget(y, 4, mimeTypeBox);
            }
            if (remoteFile.candidates == null) {
                dataTable.setWidget(y, 5, new Label("-"));
            } else {
                if (remoteFile.candidates.isEmpty()) {
                    dataTable.setWidget(y, 5, new Label(stringMessages.empty()));
                } else {

                    FlowPanel ft = new FlowPanel();
                    dataTable.setWidget(y, 5, ft);
                    for (RegattaAndRaceIdentifier candidate : remoteFile.candidates) {
                        CheckBox cb = new CheckBox(candidate.getRegattaName() + " " + candidate.getRaceName());
                        cb.setValue(true, false);
                        cb.setEnabled(!isWorking);
                        cb.addStyleName(style.style().checkboxStyle());
                        ft.add(cb);
                        cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                            @Override
                            public void onValueChange(ValueChangeEvent<Boolean> event) {
                                if (Boolean.FALSE.equals(event.getValue())) {
                                    remoteFile.candidates.remove(candidate);
                                } else {
                                    remoteFile.candidates.add(candidate);
                                }
                            }
                        });
                    }
                }
            }
            y++;
        }

        doScanButton.setEnabled(!isWorking);
        if (!isWorking) {
            statusLabel.setText("i18n idle");
        }
        center();
    }

    private void startNextLinkingRemoteTask() {
        int done = 0;
        for (RemoteFileInfo remoteFile : remoteFiles) {
            if (remoteFile.status == EStatus.WAIT_FOR_SAVE) {
                MediaTrack mediaTrack = new MediaTrack(remoteFile.url, remoteFile.url, remoteFile.startTime,
                        remoteFile.duration, remoteFile.mime, remoteFile.candidates);
                mediaService.addMediaTrack(mediaTrack, new AsyncCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        remoteFile.status = EStatus.DONE;
                        updateUI();
                        startNextLinkingRemoteTask();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        remoteFile.status = EStatus.DONE;
                        updateUI();
                        startNextLinkingRemoteTask();
                    }
                });
                if (remoteFile.status == EStatus.DONE) {
                    done++;
                }
                return;
            }
        }
        setWorking(false);
        Window.alert("i18n " + " linked " + done);
    }

    private void startNextInitializingRemoteTask() {
        doSaveButton.setEnabled(false);
        for (RemoteFileInfo remoteFile : remoteFiles) {
            statusLabel.setText(stringMessages.analyze() + ": " + remoteFile.url);
            if (remoteFile.status == EStatus.NOT_ANALYSED) {
                checkMetadata(remoteFile, new AsyncCallback<VideoMetadataDTO>() {
                    @Override
                    public void onSuccess(VideoMetadataDTO result) {
                        if (result.isDownloadable()) {
                            remoteFile.message = result.getMessage();
                            if (result.getDuration() == null) {
                                remoteFile.status = EStatus.ERROR_ANALYZE;
                            } else {
                                remoteFile.duration = result.getDuration();
                                if (result.getRecordStartedTime() != null) {
                                    remoteFile.startTime = new MillisecondsTimePoint(result.getRecordStartedTime());
                                }
                                remoteFile.mime = result.isSpherical() ? MimeType.mp4panorama : MimeType.mp4;
                                remoteFile.status = EStatus.GETTING_MEDIATRACK;
                                updateUI();
                                mediaService.getMediaTrackByUrl(remoteFile.url, new AsyncCallback<MediaTrack>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        remoteFile.status = EStatus.ERROR_ANALYZE;
                                        updateUI();
                                        startNextInitializingRemoteTask();
                                    }

                                    @Override
                                    public void onSuccess(MediaTrack result) {
                                        if (result == null) {
                                            remoteFile.status = EStatus.WAITING_FOR_LINK;
                                        } else {
                                            remoteFile.status = EStatus.ALREADY_ADDED;
                                            remoteFile.knownMediaTrack = result;
                                        }
                                        updateUI();
                                        startNextInitializingRemoteTask();
                                    }
                                });
                            }

                        } else {
                            remoteFile.status = EStatus.ERROR_DOWNLOAD;
                            updateUI();
                            startNextInitializingRemoteTask();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        remoteFile.status = EStatus.ERROR_DOWNLOAD;
                        updateUI();
                    }
                });
                return;
            }
        }
        for (RemoteFileInfo remoteFile : remoteFiles) {
            statusLabel.setText(remoteFile.url);
            if (remoteFile.status == EStatus.WAITING_FOR_LINK) {
                sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {

                    @Override
                    public void onSuccess(List<EventDTO> result) {
                        Set<RegattaAndRaceIdentifier> candidates = new HashSet<>();
                        collectAllOverlappingRaces(remoteFile, result, candidates);
                        remoteFile.status = EStatus.WAIT_FOR_SAVE;
                        remoteFile.candidates = candidates;
                        updateUI();
                        startNextInitializingRemoteTask();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        remoteFile.status = EStatus.ERROR_LINKING;
                        updateUI();
                    }
                });
                return;
            }
        }
        doSaveButton.setEnabled(true);
        setWorking(false);
    }

    private void collectAllOverlappingRaces(RemoteFileInfo remoteFile, List<EventDTO> result,
            Set<RegattaAndRaceIdentifier> candidates) {
        for (EventDTO event : result) {
            for (LeaderboardGroupDTO groups : event.getLeaderboardGroups()) {
                for (StrippedLeaderboardDTO leaderboard : groups.getLeaderboards()) {
                    for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
                        for (FleetDTO fleet : raceColumn.getFleets()) {
                            if (raceColumn.isTrackedRace(fleet)) {
                                RaceDTO race = raceColumn.getRace(fleet);
                                if (race.trackedRace != null) {
                                    GWT.log(race.getRaceIdentifier() + " " + remoteFile.startTime + " "
                                            + race.endOfRace);
                                    if (race.endOfRace == null || race.endOfRace.after(remoteFile.startTime.asDate())) {
                                        if (race.startOfRace == null || race.startOfRace.before(new Date(
                                                remoteFile.startTime.asMillis() + remoteFile.duration.asMillis()))) {
                                            candidates.add(race.getRaceIdentifier());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * For a given url that points to an mp4 video, attempts are made to parse the header, to determine the actual
     * starttime of the video and to check for a 360° flag. The video will be analyzed by the backendserver, either via
     * direct download, or proxied by the client, if a video is only available locally. If the video header cannot be
     * read, default values are used instead.
     */
    private void checkMetadata(RemoteFileInfo file, AsyncCallback<VideoMetadataDTO> asyncCallback) {
        file.status = EStatus.SERVER_ANALYSE;
        updateUI();
        // check on server first
        mediaService.checkMetadata(file.url, new AsyncCallback<VideoMetadataDTO>() {

            @Override
            public void onSuccess(VideoMetadataDTO result) {
                if (result.isDownloadable()) {
                    asyncCallback.onSuccess(result);
                } else {
                    file.status = EStatus.CLIENT_ANALYZE;
                    updateUI();
                    checkMetadataOnClient(file, asyncCallback);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                // try on client instead
                checkMetadataOnClient(file, asyncCallback);
            }

            private void checkMetadataOnClient(RemoteFileInfo file, AsyncCallback<VideoMetadataDTO> asyncCallback) {
                JSDownloadUtils.getData(file.url, new JSDownloadCallback() {

                    @Override
                    public void progress(Double current, Double total) {
                        statusLabel.setText(stringMessages.transferStarted() + " " + Math.round(current / 1024 / 1024)
                                + "/" + Math.round(total / 1024 / 1024) + " MB");
                    }

                    @Override
                    public void error(Object msg) {
                        asyncCallback.onSuccess(
                                new VideoMetadataDTO(false, null, false, null, msg == null ? "" : msg.toString()));
                    }

                    @Override
                    public void complete(Int8Array start, Int8Array end, Double skipped) {
                        statusLabel.setText(stringMessages.analyze());
                        byte[] jStart = new byte[start.byteLength()];
                        for (int i = 0; i < start.byteLength(); i++) {
                            jStart[i] = start.get(i);
                        }
                        byte[] jEnd = new byte[end.byteLength()];
                        for (int i = 0; i < end.byteLength(); i++) {
                            jEnd[i] = end.get(i);
                        }
                        // Due to js represeting everything as 64double, the max safe file is around 4 petabytes
                        mediaService.checkMetadata(jStart, jEnd, skipped.longValue(), asyncCallback);
                    }
                });
            }
        });
    }

    private String asString(EStatus status) {
        switch (status) {
        case NOT_ANALYSED:
            return "18n not analysied";
        default:
            return status.name();
        }
    }

    protected void retrieveRemoteFileList(String url) {
        remoteFiles.clear();
        dataTable.clear();
        updateUI();

        JSDownloadUtils.getFileList(url, new JSHrefCallback() {
            @Override
            public void newHref(String foundLink) {
                if (foundLink.equalsIgnoreCase(url)) {
                    return;
                }
                // nginx dummy file in video folder
                if (foundLink.equalsIgnoreCase(
                        "If%20you%20only%20see%20this%20file%2C%20you%20need%20to%20copy%20your%20files%20to%20place_videos_here%20folder")) {
                    return;
                }
                if (foundLink.startsWith("..")) {
                    return;
                }
                if (foundLink.startsWith("./")) {
                    foundLink = foundLink.substring(2);
                }
                if (!foundLink.startsWith("http://") || foundLink.startsWith("https://")) {
                    if (!url.endsWith("/")) {
                        foundLink = "/" + foundLink;
                    }
                    foundLink = url + foundLink;
                }
                remoteFiles.add(new RemoteFileInfo(foundLink));
            }

            @Override
            public void noResult() {
                Window.alert(stringMessages.serverURLInvalid());
            }

            @Override
            public void complete() {
                updateUI();
                startNextInitializingRemoteTask();
            }
        });
    }

    static interface StyleHolder extends ClientBundle {
        @Source("MultiVideoDialog.css")
        Style style();
    }

    static interface Style extends CssResource {
        String tableStyle();

        String checkboxStyle();
    }

    enum EStatus {
        NOT_ANALYSED,
        WAITING_FOR_LINK,
        SERVER_ANALYSE,
        ERROR_ANALYZE,
        CLIENT_ANALYZE,
        ERROR_DOWNLOAD,
        ERROR_LINKING,
        WAIT_FOR_SAVE,
        DONE,
        ALREADY_ADDED,
        GETTING_MEDIATRACK;
    }

    static class RemoteFileInfo {
        protected MediaTrack knownMediaTrack;
        protected Set<RegattaAndRaceIdentifier> candidates;
        protected MimeType mime;
        protected String message;
        protected TimePoint startTime;
        protected Duration duration;
        protected EStatus status = EStatus.NOT_ANALYSED;;
        final String url;

        public RemoteFileInfo(String foundLink) {
            this.url = foundLink;
        }

    }
}
