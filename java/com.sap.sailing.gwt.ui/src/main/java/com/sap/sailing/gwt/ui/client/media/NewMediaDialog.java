package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.user.client.Window;
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
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.YoutubeApi;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class NewMediaDialog extends DataEntryDialog<MediaTrack> {

    private static final boolean DONT_FIRE_EVENTS = false;

    private static final Validator<MediaTrack> MEDIA_TRACK_VALIDATOR = new Validator<MediaTrack>() {

        @Override
        public String getErrorMessage(MediaTrack valueToValidate) {
            return null;
        }

    };

    protected final StringMessages stringMessages;

    protected MediaTrack mediaTrack = new MediaTrack();

    private TextBox urlBox;

    private TextBox titleBox;

    protected TextBox startTimeBox;

    private SimplePanel infoLabel; // showing either mime type or youtube id

    private TextBox durationBox;

    private TimePoint defaultStartTime;

    private Label infoLabelLabel;

    final private RegattaAndRaceIdentifier raceIdentifier;

    final private MediaServiceAsync mediaService;

    private boolean remoteMp4WasStarted;
    private boolean remoteMp4WasFinished;

    private Button defaultTimeButton;

    public NewMediaDialog(MediaServiceAsync mediaService, TimePoint defaultStartTime, StringMessages stringMessages,
            RegattaAndRaceIdentifier raceIdentifier, DialogCallback<MediaTrack> dialogCallback) {
        super(stringMessages.addMediaTrack(), "", stringMessages.ok(), stringMessages.cancel(), MEDIA_TRACK_VALIDATOR,
                dialogCallback);
        this.defaultStartTime = defaultStartTime != null ? defaultStartTime : MillisecondsTimePoint.now();
        this.stringMessages = stringMessages;
        this.raceIdentifier = raceIdentifier;
        this.mediaService = mediaService;
        registerNativeMethods();
    }

    @Override
    protected MediaTrack getResult() {
        // mediaTrack.url = urlBox.getValue();
        mediaTrack.title = titleBox.getValue();
        updateStartTimeFromUi();
        updateDurationFromUi();
        connectMediaWithRace();

        return mediaTrack;
    }

    private void updateDurationFromUi() {
        String duration = durationBox.getValue();
        if (duration != null && !duration.trim().isEmpty()) {
            mediaTrack.duration = TimeFormatUtil.hrsMinSecToMilliSeconds(duration);
        } else {
            mediaTrack.duration = null;
        }
    }

    protected void connectMediaWithRace() {
        Set<RegattaAndRaceIdentifier> assignedRaces = new HashSet<RegattaAndRaceIdentifier>();
        assignedRaces.add(this.raceIdentifier);
        mediaTrack.assignedRaces = assignedRaces;
    }

    protected void updateStartTimeFromUi() {
        String startTimeText = startTimeBox.getValue();
        if (startTimeText != null && !startTimeText.equals("")) {
            try {
                Date startTime = TimeFormatUtil.DATETIME_FORMAT.parse(startTimeText);
                mediaTrack.startTime = new MillisecondsTimePoint(startTime);
            } catch (IllegalArgumentException ex) {
                // TODO: highlight format error in UI
            }
        }
    }

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
		mediaElement
				.addEventListener(
						'loadedmetadata',
						function() {
							that.@com.sap.sailing.gwt.ui.client.media.NewMediaDialog::loadedmetadata(Lcom/google/gwt/dom/client/MediaElement;)(mediaElement);
						});
    }-*/;

    public void loadedmetadata(MediaElement mediaElement) {
        mediaTrack.startTime = this.defaultStartTime;
        double duration = mediaElement.getDuration();
        if (duration > 0) {
            mediaTrack.duration = new MillisecondsDurationImpl((long) Math.round(duration * 1000));
        } else {
            mediaTrack.duration = null;
        }
        refreshUI();
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(5, 2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, new Label(stringMessages.url() + ":"));
        urlBox = createUrlBox();
        formGrid.setWidget(0, 1, urlBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.name() + ":"));
        titleBox = createTextBox(null);
        formGrid.setWidget(1, 1, titleBox);
        infoLabelLabel = new Label();
        formGrid.setWidget(2, 0, infoLabelLabel);
        infoLabel = new SimplePanel();
        infoLabel.setWidget(new Label(""));
        formGrid.setWidget(2, 1, infoLabel);
        formGrid.setWidget(3, 0, new Label(stringMessages.startTime() + ":"));
        startTimeBox = createTextBox(null);
        FlowPanel startTimePanel = new FlowPanel();
        startTimePanel.add(startTimeBox);
        defaultTimeButton = new Button(StringMessages.INSTANCE.resetStartTimeToDefault());
        defaultTimeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mediaTrack.startTime = defaultStartTime;
                refreshUI();
            }
        });
        startTimePanel.add(defaultTimeButton);
        formGrid.setWidget(3, 1, startTimePanel);
        formGrid.setWidget(4, 0, new Label(stringMessages.duration() + ":"));
        durationBox = createTextBox(null);
        formGrid.setWidget(4, 1, durationBox);
        mainPanel.add(formGrid);

        return mainPanel;
    }

    private TextBox createUrlBox() {
        TextBox result = createTextBox(null);
        result.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        updateFromUrl();
                    }
                });
            }
        });
        return result;
    }

    protected void updateFromUrl() {
        String url = urlBox.getValue();
        String youtubeId = YoutubeApi.getIdByUrl(url);
        if (youtubeId != null) {
            mediaTrack.url = youtubeId;
            mediaTrack.mimeType = MimeType.youtube;
            loadYoutubeMetadata(youtubeId);
        } else {
            mediaTrack.url = url;
            loadMediaDuration();
            String simpleUrl = sliceBefore(mediaTrack.url, "?");
            simpleUrl = sliceBefore(simpleUrl, "#");
            String lastPathSegment = simpleUrl.substring(simpleUrl.lastIndexOf('/') + 1);
            int dotPos = lastPathSegment.lastIndexOf('.');
            if (dotPos >= 0) {
                mediaTrack.title = lastPathSegment.substring(0, dotPos);
                String fileEnding = lastPathSegment.substring(dotPos + 1).toLowerCase();
                mediaTrack.mimeType = MimeType.byName(fileEnding);
            } else {
                mediaTrack.title = mediaTrack.url;
                mediaTrack.mimeType = null;
            }
        }
        remoteMp4WasStarted = false;
        remoteMp4WasFinished = false;
        refreshUI();
    }

    private String sliceBefore(String lastPathSegment, String slicer) {
        int paramSegment = lastPathSegment.indexOf(slicer);
        if (paramSegment > 0) {
            return lastPathSegment.substring(0, paramSegment);
        }
        return lastPathSegment;
    }

    protected void setUiEnabled(boolean isEnabled) {
        urlBox.setEnabled(isEnabled);
        defaultTimeButton.setEnabled(isEnabled);
        startTimeBox.setEnabled(isEnabled);
        getOkButton().setEnabled(isEnabled);
        if (isEnabled) {
            setCursor(Style.Cursor.AUTO);
        } else {
            setCursor(Style.Cursor.WAIT);
        }
    }

    private native void registerNativeMethods() /*-{
		var that = this;
		window.youtubeMetadataCallback = function(metadata) {
			var title = metadata.entry.media$group.media$title.$t;
			var duration = metadata.entry.media$group.yt$duration.seconds;
			var description = metadata.entry.media$group.media$description.$t;
			that.@com.sap.sailing.gwt.ui.client.media.NewMediaDialog::youtubeMetadataCallback(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(title, duration, description);
		}
    }-*/;

    /**
     * Inspired by https://developers.google.com/web-toolkit/doc/latest/tutorial/Xsite
     * 
     * @param youtubeId
     */
    public native void loadYoutubeMetadata(String youtubeId) /*-{
		var that = this;

		//Create temporary script element.
		window.youtubeMetadataCallbackScript = document.createElement("script");
		window.youtubeMetadataCallbackScript.src = "http://gdata.youtube.com/feeds/api/videos/"
				+ youtubeId
				+ "?alt=json&orderby=published&format=6&callback=youtubeMetadataCallback";
		document.body.appendChild(window.youtubeMetadataCallbackScript);

		// Cancel meta data capturing after has 2-seconds timeout.
		setTimeout(
				function() {
					//Remove temporary script element.
					if (window != null && window.youtubeMetadataCallbackScript != null) {
					    document.body
							.removeChild(window.youtubeMetadataCallbackScript);
							
					    delete window.youtubeMetadataCallbackScript;
					}
					that.@com.sap.sailing.gwt.ui.client.media.NewMediaDialog::setUiEnabled(Z)(true);
				}, 2000);

    }-*/;

    public void youtubeMetadataCallback(String title, String durationInSeconds, String description) {
        setUiEnabled(true);
        mediaTrack.title = title;
        try {
            long duration = (long) Math.round(1000 * Double
                    .valueOf(durationInSeconds));
            if (duration > 0) {
                mediaTrack.duration = new MillisecondsDurationImpl(duration);
            } else {
                mediaTrack.duration = null;
            }
        } catch (NumberFormatException ex) {
            mediaTrack.duration = null;
        }
        mediaTrack.startTime = this.defaultStartTime;
        refreshUI();
    }

    private void refreshUI() {
        titleBox.setValue(mediaTrack.title, DONT_FIRE_EVENTS);
        if (mediaTrack.isYoutube()) {
            infoLabelLabel.setText(stringMessages.youtubeId() + ":");
            infoLabel.setWidget(new Label(mediaTrack.url));
        } else {
            infoLabelLabel.setText(stringMessages.mimeType() + ":");
            if (mediaTrack.mimeType == MimeType.mp4 || mediaTrack.mimeType == MimeType.mp4panorama) {
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
        TimePoint startTime = mediaTrack.startTime != null ? mediaTrack.startTime : defaultStartTime; 
        String startTimeText = TimeFormatUtil.DATETIME_FORMAT.format(startTime.asDate());

        startTimeBox.setText(startTimeText);
        updateStartTimeFromUi();
        if (mediaTrack.duration != null) {
            durationBox.setText(TimeFormatUtil.durationToHrsMinSec(mediaTrack.duration));
        } else {
            durationBox.setText("");
        }
    }

    private void processMp4(MediaTrack mediaTrack) {
        this.setUiEnabled(false);
        remoteMp4WasStarted = true;
        remoteMp4WasFinished = false;
        infoLabel.setWidget(new Label(stringMessages.processingMP4()));
        mediaService.checkMetadata(mediaTrack.url, new AsyncCallback<VideoMetadataDTO>() {

            @Override
            public void onSuccess(VideoMetadataDTO result) {
                setUiEnabled(true);
                remoteMp4WasFinished = true;
                mp4MetadataResult(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                setUiEnabled(true);
                remoteMp4WasFinished = true;
                manualMimeTypeSelection(caught.getMessage(), mediaTrack);
            }
        });
    }

    protected void mp4MetadataResult(VideoMetadataDTO result) {
        if (!result.isDownloadable()) {
            Window.alert("Could not download file " + mediaTrack.url);
            manualMimeTypeSelection(result.getMessage(), mediaTrack);
        } else {
            mediaTrack.mimeType = result.isSpherical() ? MimeType.mp4panorama : MimeType.mp4;
            mediaTrack.startTime = new MillisecondsTimePoint(result.getRecordStartedTime());
            refreshUI();
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
        mimeTypeListBox.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                mediaTrack.mimeType = MimeType.valueOf(mimeTypeListBox.getSelectedValue());
            }
        });
        mimeTypeListBox.setSelectedIndex(MimeType.mp4.equals(mediaTrack.mimeType)?0:1);
        fp.add(mimeTypeListBox);
        infoLabel.setWidget(fp);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return urlBox;
    }
}
