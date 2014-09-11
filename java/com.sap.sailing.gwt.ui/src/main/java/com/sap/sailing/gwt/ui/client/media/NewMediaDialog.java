package com.sap.sailing.gwt.ui.client.media;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsDurationImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.gwt.ui.adminconsole.RegattasAndRacesDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.YoutubeApi;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class NewMediaDialog extends DataEntryDialog<MediaTrack> {

    private static final boolean DONT_FIRE_EVENTS = false;

    private static final Validator<MediaTrack> MEDIA_TRACK_VALIDATOR = new Validator<MediaTrack>() {

        @Override
        public String getErrorMessage(MediaTrack valueToValidate) {
            return null;
        }

    };

    private final StringMessages stringMessages;

    private MediaTrack mediaTrack = new MediaTrack();

    private TextBox urlBox;

    private TextBox titleBox;

    private TextBox startTimeBox;

    private Label infoLabel; // showing either mime type or youtube id

    private TextBox durationBox;

    private TimePoint defaultStartTime;

    private Label infoLabelLabel;

    private SailingServiceAsync sailingService;

    private ErrorReporter errorReporter;

    private RegattaRefresher regattaRefresher;

    private Set<RegattasDisplayer> regattasDisplayers;
    
    private Widget listOfRacesForMedia;

    private RegattasAndRacesDialog racesForMediaDialog;
    
    private boolean searchForRaces = false;

    private RegattaAndRaceIdentifier raceIdentifier;

    public NewMediaDialog(TimePoint defaultStartTime, StringMessages stringMessages,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, RegattaRefresher regattaRefresher,
            Set<RegattasDisplayer> regattasDisplayers, DialogCallback<MediaTrack> dialogCallback) {
        this(defaultStartTime, stringMessages, null, dialogCallback);
        this.searchForRaces = true;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.regattasDisplayers = regattasDisplayers;
    }

    public NewMediaDialog(TimePoint defaultStartTime, StringMessages stringMessages,
            RegattaAndRaceIdentifier raceIdentifier, DialogCallback<MediaTrack> dialogCallback) {
        super(stringMessages.addMediaTrack(), "", stringMessages.ok(), stringMessages.cancel(), MEDIA_TRACK_VALIDATOR,
                dialogCallback);
        this.defaultStartTime = defaultStartTime;
        this.stringMessages = stringMessages;
        this.raceIdentifier = raceIdentifier;
        registerNativeMethods();
    }

    @Override
    protected MediaTrack getResult() {
        // mediaTrack.url = urlBox.getValue();
        mediaTrack.title = titleBox.getValue();
        getStartTimeAndRefreshRacesForMedia();
        String duration = durationBox.getValue();
        if (duration != null && !duration.equals("")) {
            mediaTrack.duration = TimeFormatUtil.hrsMinSecToMilliSeconds(duration);
        }

        connectMediaWithRace();
        
        return mediaTrack;
    }

    private void connectMediaWithRace() {
        if(searchForRaces){
            mediaTrack.regattasAndRaces = racesForMediaDialog.getSelectedRegattasAndRaces();
        }else{
            Set<RegattaAndRaceIdentifier> regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
            regattasAndRaces.add(this.raceIdentifier);
            mediaTrack.regattasAndRaces = regattasAndRaces;
        }
    }

    private void getStartTimeAndRefreshRacesForMedia() {
        try {
            String startTime = startTimeBox.getValue();
            if (startTime != null && !startTime.equals("")) {
                mediaTrack.startTime = new MillisecondsTimePoint(TimeFormatUtil.DATETIME_FORMAT.parse(startTime));
                if(searchForRaces){
                    regattaRefresher.fillRegattas();
                    listOfRacesForMedia.setVisible(true);
                }
            }
        } catch (Exception e) {
            if(searchForRaces){
                listOfRacesForMedia.setVisible(false);
            }
        }
    }
    
    private Widget racesForMedia(){
        racesForMediaDialog = new RegattasAndRacesDialog(sailingService, mediaTrack,
                errorReporter, regattaRefresher, stringMessages, null,
                new DialogCallback<Set<RegattaAndRaceIdentifier>>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(Set<RegattaAndRaceIdentifier> regattas) {
                        if (regattas.size() >= 0) {
                            mediaTrack.regattasAndRaces.clear();
                            mediaTrack.regattasAndRaces.addAll(regattas);
                        }
                    }
                });
        racesForMediaDialog.ensureDebugId("RegattasAndRacesDialog");
        regattasDisplayers.add(racesForMediaDialog);
        
        return listOfRacesForMedia = racesForMediaDialog.getAdditionalWidget();
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
        mediaTrack.duration = new MillisecondsDurationImpl((long) Math.round(mediaElement.getDuration() * 1000));
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
        infoLabel = new Label();
        formGrid.setWidget(2, 1, infoLabel);
        formGrid.setWidget(3, 0, new Label(stringMessages.startTime() + ":"));
        startTimeBox = createTextBox(null);
        formGrid.setWidget(3, 1, startTimeBox);
        formGrid.setWidget(4, 0, new Label(stringMessages.duration() + ":"));
        durationBox = createTextBox(null);
        formGrid.setWidget(4, 1, durationBox);
        mainPanel.add(formGrid);
        
        if(searchForRaces){
            mainPanel.add(racesForMedia());
            listOfRacesForMedia.setVisible(false);
        }
        return mainPanel;
    }

    private TextBox createUrlBox() {
        TextBox result = createTextBox(null);
        result.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                updateFromUrl();
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
            setUiEnabled(false);
        } else {
            mediaTrack.url = url;
            loadMediaDuration();

            String lastPathSegment = mediaTrack.url.substring(mediaTrack.url.lastIndexOf('/') + 1);
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

        refreshUI();
    }

    protected void setUiEnabled(boolean isEnabled) {
        urlBox.setEnabled(isEnabled);
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
					document.body
							.removeChild(window.youtubeMetadataCallbackScript);
					delete window.youtubeMetadataCallbackScript;
					that.@com.sap.sailing.gwt.ui.client.media.NewMediaDialog::setUiEnabled(Z)(true);
				}, 2000);

    }-*/;

    public void youtubeMetadataCallback(String title, String durationInSeconds, String description) {
        setUiEnabled(true);
        mediaTrack.title = title;
        try {
            mediaTrack.duration = new MillisecondsDurationImpl((long) Math.round(1000 * Double
                    .valueOf(durationInSeconds)));
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
            infoLabel.setText(mediaTrack.url);
        } else {
            infoLabelLabel.setText(stringMessages.mimeType() + ":");
            infoLabel.setText(mediaTrack.typeToString());
        }
        String startTimeText = mediaTrack.startTime == null ? "undefined" : TimeFormatUtil.DATETIME_FORMAT
                .format(mediaTrack.startTime.asDate());
        
        startTimeBox.setText(startTimeText);
        getStartTimeAndRefreshRacesForMedia();
        durationBox.setText(TimeFormatUtil.durationToHrsMinSec(mediaTrack.duration));
    }

    @Override
    public void show() {
        super.show();
        urlBox.setFocus(true);
    }
}
