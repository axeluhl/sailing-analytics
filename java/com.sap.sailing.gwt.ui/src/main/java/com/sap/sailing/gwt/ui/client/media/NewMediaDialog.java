package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.adminconsole.FileStorageServiceConnectionTestObservable;
import com.sap.sailing.gwt.ui.adminconsole.FileStorageServiceConnectionTestObservable.FileStorageServiceConnectionTestObserver;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback;
import com.sap.sailing.gwt.ui.common.client.YoutubeApi;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class NewMediaDialog extends DataEntryDialog<MediaTrack> implements FileStorageServiceConnectionTestObserver {

    private static final boolean DONT_FIRE_EVENTS = false;

    protected static class MediaTrackValidator implements Validator<MediaTrack> {
        private final StringMessages stringMessages;

        public MediaTrackValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(MediaTrack media) {
            String errorMessage = null;
            if (media.url == null || media.url.trim().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrl();
            } else if (media.title == null || media.title.trim().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterA(stringMessages.title());
            } else if (media.mimeType == null) {
                errorMessage = stringMessages.pleaseEnterA(stringMessages.mimeType());
            } else if (media.startTime == null) {
                errorMessage = stringMessages.pleaseEnterA(stringMessages.startTime());
            } else if (media.duration == null) {
                errorMessage = stringMessages.pleaseEnterA(stringMessages.duration());
            }
            return errorMessage;
        }
    }

    protected final StringMessages stringMessages;

    protected MediaTrack mediaTrack = new MediaTrack();

    private URLFieldWithFileUpload urlBox;

    private TextBox titleBox;

    protected DateAndTimeInput startTimeBox;

    private SimplePanel infoLabel; // showing either mime type or youtube id

    private TextBox durationBox;

    private TimePoint defaultStartTime;

    private Label infoLabelLabel;

    final private RegattaAndRaceIdentifier raceIdentifier;
    final private Set<RegattaAndRaceIdentifier> assignedRaces;

    final private MediaServiceAsync mediaService;

    private boolean remoteMp4WasStarted;
    private boolean remoteMp4WasFinished;

    private Button defaultTimeButton;

    private SimpleBusyIndicator busyIndicator;

    private String lastCheckedUrl;

    private boolean manuallyEditedStartTime = false;

    public NewMediaDialog(MediaServiceAsync mediaService, TimePoint defaultStartTime, StringMessages stringMessages,
            RegattaAndRaceIdentifier raceIdentifier, FileStorageServiceConnectionTestObservable storageServiceConnection,
            DialogCallback<MediaTrack> dialogCallback) {
        super(stringMessages.addMediaTrack(), "", stringMessages.ok(), stringMessages.cancel(),
                new MediaTrackValidator(stringMessages), dialogCallback);
        this.defaultStartTime = defaultStartTime != null ? defaultStartTime : MillisecondsTimePoint.now();
        this.stringMessages = stringMessages;
        this.raceIdentifier = raceIdentifier;
        this.assignedRaces = new HashSet<RegattaAndRaceIdentifier>();
        this.assignedRaces.add(raceIdentifier);
        this.mediaService = mediaService;

        urlBox = new URLFieldWithFileUpload(stringMessages, false);
        urlBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validateAndUpdate();
            }
        });
        titleBox = createTextBox(null);
        titleBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                mediaTrack.title = titleBox.getValue();
                validateAndUpdate();
            }
        });
        startTimeBox = new DateAndTimeInput(Accuracy.MILLISECONDS);
        startTimeBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                manuallyEditedStartTime = true;
                mediaTrack.startTime = new MillisecondsTimePoint(event.getValue());
                validateAndUpdate();
            }
        });
        defaultTimeButton = new Button(StringMessages.INSTANCE.resetStartTimeToDefault());
        defaultTimeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                manuallyEditedStartTime = true;
                mediaTrack.startTime = defaultStartTime;
                refreshUI(); // Force UI update
                validateAndUpdate();
            }
        });
        durationBox = createTextBox(null);
        durationBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String duration = durationBox.getValue();
                if (duration != null && !duration.trim().isEmpty()) {
                    try {
                        mediaTrack.duration = TimeFormatUtil.hrsMinSecToMilliSeconds(duration);
                    } catch (NumberFormatException e) {
                        mediaTrack.duration = null;
                    }
                } else {
                    mediaTrack.duration = null;
                }
                validateAndUpdate();
            }
        });

        storageServiceConnection.registerObserver(this);
    }

    @Override
    protected MediaTrack getResult() {
        updateFromUrl();
        connectMediaWithRace();
        return mediaTrack;
    }
    
    protected void connectMediaWithRace() {
        mediaTrack.assignedRaces = assignedRaces;
    }

    protected void updateFromUrl() {
        String url = urlBox.getValue();
        if (url != null && !url.isEmpty()) {
            boolean urlChanged = !url.equals(lastCheckedUrl);
            lastCheckedUrl = url;
            if (urlChanged) {
                remoteMp4WasStarted = false;
                remoteMp4WasFinished = false;
                manuallyEditedStartTime = false;

                if (mediaTrack.startTime == null) {
                    mediaTrack.startTime = defaultStartTime;
                }

                String youtubeId = YoutubeApi.getIdByUrl(url);
                if (youtubeId != null) {
                    mediaTrack.url = youtubeId;
                    mediaTrack.mimeType = MimeType.youtube;
                    mediaService.checkYoutubeMetadata(youtubeId, new AsyncCallback<VideoMetadataDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            infoLabel.setWidget(new Label(caught.getMessage()));
                        }
                        @Override
                        public void onSuccess(VideoMetadataDTO result) {
                            if (result.isDownloadable()) {
                                mediaTrack.duration = result.getDuration();
                                mediaTrack.title = result.getMessage();
                                refreshUI();
                                validateAndUpdate();
                            } else {
                                infoLabel.setWidget(new Label(result.getMessage()));
                            }
                        }
                    });
                } else {
                    mediaTrack.url = url;
                    AnchorElement anchor = Document.get().createAnchorElement();
                    anchor.setHref(url);
                    //remove trailing / as well
                    String lastPathSegment = anchor.getPropertyString("pathname").substring(1);
                    int dotPos = lastPathSegment.lastIndexOf('.');
                    if (dotPos >= 0) {
                        mediaTrack.title = lastPathSegment.substring(0, dotPos);
                        String fileEnding = lastPathSegment.substring(dotPos + 1).toLowerCase();
                        mediaTrack.mimeType = MimeType.byName(fileEnding);
                        if (MimeType.mp4.equals(mediaTrack.mimeType)) {
                            processMp4(mediaTrack);
                        } else {
                            loadMediaDuration();
                        }
                    } else {
                        mediaTrack.title = mediaTrack.url;
                        mediaTrack.mimeType = null;
                    }
                }
                refreshUI();
            }
        }
    }

    //used for audio only tracks, using native mediaelement to determine time
    private void loadMediaDuration() {
        MediaBase mediaBase = Audio.createIfSupported();
        if (mediaBase != null) {
            addLoadMetadataHandler(mediaBase.getMediaElement());
            mediaBase.setPreload(MediaElement.PRELOAD_METADATA);
            mediaBase.setSrc(mediaTrack.url);
            mediaBase.load();
        }
    }

    native void addLoadMetadataHandler(MediaElement mediaElement) /*-{
        var that = this;
        mediaElement.addEventListener('loadedmetadata',
            function() {
                that.@com.sap.sailing.gwt.ui.client.media.NewMediaDialog::loadedmetadata(Lcom/google/gwt/dom/client/MediaElement;)(mediaElement);
            });
    }-*/;

    public void loadedmetadata(MediaElement mediaElement) {
        if (!manuallyEditedStartTime) {
            mediaTrack.startTime = this.defaultStartTime;
        }
        double duration = mediaElement.getDuration();
        if (duration > 0) {
            mediaTrack.duration = new MillisecondsDurationImpl((long) Math.round(duration * 1000));
        } else {
            mediaTrack.duration = null;
        }
        refreshUI();
        validateAndUpdate();
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(6, 2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, new Label(stringMessages.url() + ":"));
        formGrid.setWidget(0, 1, urlBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.name() + ":"));
        formGrid.setWidget(1, 1, titleBox);

        infoLabelLabel = new Label();
        formGrid.setWidget(2, 0, infoLabelLabel);
        infoLabel = new SimplePanel();
        infoLabel.setWidget(new Label(""));
        formGrid.setWidget(2, 1, infoLabel);
        formGrid.setWidget(3, 0, new Label(stringMessages.startTime() + ":"));

        FlowPanel startTimePanel = new FlowPanel();
        startTimePanel.add(startTimeBox);
        startTimePanel.add(defaultTimeButton);
        formGrid.setWidget(3, 1, startTimePanel);

        formGrid.setWidget(4, 0, new Label(stringMessages.duration() + ":"));
        formGrid.setWidget(4, 1, durationBox);
        busyIndicator = new SimpleBusyIndicator();
        formGrid.setWidget(5, 0, busyIndicator);
        mainPanel.add(formGrid);
        return mainPanel;
    }

    private String sliceBefore(String lastPathSegment, String slicer) {
        int paramSegment = lastPathSegment.indexOf(slicer);
        if (paramSegment > 0) {
            return lastPathSegment.substring(0, paramSegment);
        }
        return lastPathSegment;
    }

    public void setBusy(boolean busy) {
        busyIndicator.setBusy(busy);
    }

    protected void refreshUI() {
        titleBox.setValue(mediaTrack.title, DONT_FIRE_EVENTS);
        if (mediaTrack.isYoutube()) {
            infoLabelLabel.setText(stringMessages.youtubeId() + ":");
            infoLabel.setWidget(new Label(mediaTrack.url));
        } else {
            infoLabelLabel.setText(stringMessages.mimeType() + ":");
            if (mediaTrack.mimeType == MimeType.mp4 || mediaTrack.mimeType == MimeType.mp4panorama || mediaTrack.mimeType == MimeType.mp4panoramaflip) {
                if (!remoteMp4WasStarted) {
                    processMp4(mediaTrack);
                } else if (remoteMp4WasFinished) {
                    manualMimeTypeSelection(null, mediaTrack);
                } else {
                    infoLabel.setWidget(new Label(stringMessages.processingMP4()));
                }
            } else {
                infoLabel.setWidget(new Label(mediaTrack.typeToString()));
            }
        }
        startTimeBox.setValue(mediaTrack.startTime == null ? null : mediaTrack.startTime.asDate(), DONT_FIRE_EVENTS);
        if (mediaTrack.duration != null) {
            durationBox.setValue(TimeFormatUtil.durationToHrsMinSec(mediaTrack.duration), DONT_FIRE_EVENTS);
        } else {
            durationBox.setValue("", DONT_FIRE_EVENTS);
        }
    }

    @Override
    public void onFileStorageServiceTestPassed() {
        urlBox.setUploadEnabled(true);
    }

    private void processMp4(MediaTrack mediaTrack) {
        this.busyIndicator.setBusy(true);
        remoteMp4WasStarted = true;
        remoteMp4WasFinished = false;
        Label infoLbl = new Label(stringMessages.processingMP4());
        infoLabel.setWidget(infoLbl);
        checkMetadata(mediaTrack.url, infoLbl, new AsyncCallback<VideoMetadataDTO>() {
            @Override
            public void onSuccess(VideoMetadataDTO result) {
                busyIndicator.setBusy(false);
                remoteMp4WasFinished = true;
                mp4MetadataResult(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                busyIndicator.setBusy(false);
                remoteMp4WasFinished = true;
                manualMimeTypeSelection(caught.getMessage(), mediaTrack);
            }
        });
    }

    /**
     * For a given url that points to an mp4 video, attempts are made to parse the header, to determine the actual
     * starttime of the video and to check for a 360° flag. The video will be analyzed by the backendserver, either via
     * direct download, or proxied by the client, if a video is only available locally. If the video header cannot be
     * read, default values are used instead.
     */
    private void checkMetadata(String url, Label lbl, AsyncCallback<VideoMetadataDTO> asyncCallback) {
        // check on server first
        mediaService.checkMetadata(mediaTrack.url, new AsyncCallback<VideoMetadataDTO>() {
            @Override
            public void onSuccess(VideoMetadataDTO result) {
                remoteMp4WasFinished = true;
                if (result.isDownloadable()) {
                    busyIndicator.setBusy(false);
                    mp4MetadataResult(result);
                } else {
                    checkMetadataOnClient(url, lbl, asyncCallback);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                // try on client instead
                checkMetadataOnClient(url, lbl, asyncCallback);
            }

            private void checkMetadataOnClient(String url, Label lbl, AsyncCallback<VideoMetadataDTO> asyncCallback) {
                JSDownloadUtils.getData(url, new JSDownloadCallback() {
                    @Override
                    public void progress(Double current, Double total) {
                        lbl.setText(stringMessages.transferStarted() + " " + Math.round(current / 1024 / 1024) + "/"
                                + Math.round(total / 1024 / 1024) + " MB");
                        infoLabel.setWidget(lbl);
                    }

                    @Override
                    public void error(Object msg) {
                        asyncCallback
                                .onSuccess(new VideoMetadataDTO(false, null, false, null, msg == null ? "" : msg.toString()));
                    }

                    @Override
                    public void complete(Int8Array start, Int8Array end, Double skipped) {
                        lbl.setText(stringMessages.analyze());
                        infoLabel.setWidget(lbl);
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

    protected void mp4MetadataResult(VideoMetadataDTO result) {
        if (!result.isDownloadable()) {
            Notification.notify(stringMessages.couldNotDownload(mediaTrack.url), NotificationType.ERROR);
            manualMimeTypeSelection(result.getMessage(), mediaTrack);
        } else {
            mediaTrack.duration = result.getDuration();
            if (mediaTrack.duration == null) {
                loadMediaDuration(); // Attempt duration detection with audio channel
            }
            mediaTrack.mimeType = result.isSpherical() ? MimeType.mp4panorama : MimeType.mp4;
            if (result.getRecordStartedTime() != null && !manuallyEditedStartTime) {
                mediaTrack.startTime = new MillisecondsTimePoint(result.getRecordStartedTime());
            }
            refreshUI();
            validateAndUpdate();
        }
    }

    private void manualMimeTypeSelection(String message, MediaTrack mediaTrack) {
        FlowPanel fp = new FlowPanel();
        if (message != null) {
            fp.add(new Label(message));
        }
        ListBox mimeTypeListBox = createListBox(false);
        mimeTypeListBox.addItem(MimeType.mp4.name());
        mimeTypeListBox.addItem(MimeType.mp4panorama.name());
        mimeTypeListBox.addItem(MimeType.mp4panoramaflip.name());
        mimeTypeListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                mediaTrack.mimeType = MimeType.valueOf(mimeTypeListBox.getSelectedValue());
            }
        });
        mimeTypeListBox.setSelectedIndex(MimeType.mp4 == mediaTrack.mimeType ? 0 : 1);
        fp.add(mimeTypeListBox);
        infoLabel.setWidget(fp);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return urlBox.getInitialFocusWidget();
    }
}
