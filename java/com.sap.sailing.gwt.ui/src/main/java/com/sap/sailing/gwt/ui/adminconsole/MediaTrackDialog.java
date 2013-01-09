package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MimeType;

public class MediaTrackDialog extends DataEntryDialog<MediaTrack> {

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

    private Label startTimeLabel;

    private Label mimeTypeLabel;
    
    private Label durationLabel;

    public MediaTrackDialog(StringMessages stringMessages, DialogCallback<MediaTrack> dialogCallback) {
        super("Media Track", "", stringMessages.ok(), stringMessages.cancel(), MEDIA_TRACK_VALIDATOR, dialogCallback);
        this.stringMessages = stringMessages;
    }

    @Override
    protected MediaTrack getResult() {
        mediaTrack.url = urlBox.getValue();
        mediaTrack.title = titleBox.getValue();
        return mediaTrack;
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
        mediaElement.addEventListener('loadedmetadata', function() {
            that.@com.sap.sailing.gwt.ui.adminconsole.MediaTrackDialog::loadedmetadata(Lcom/google/gwt/dom/client/MediaElement;)(mediaElement);
        });
    }-*/;
    
    public void loadedmetadata(MediaElement mediaElement) {
        double startOffsetTime = mediaElement.getStartOffsetTime();
        mediaTrack.startTime = Double.isNaN(startOffsetTime) ? null : new Date(Math.round(startOffsetTime * 1000));
        mediaTrack.durationInMillis = (int) Math.round(mediaElement.getDuration() * 1000);
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
        formGrid.setWidget(2, 0, new Label(stringMessages.mimeType() + ":"));
        mimeTypeLabel = new Label();
        formGrid.setWidget(2, 1, mimeTypeLabel);
        formGrid.setWidget(3, 0, new Label(stringMessages.startTime() + ":"));
        startTimeLabel = new Label();
        formGrid.setWidget(3, 1, startTimeLabel);
        formGrid.setWidget(4, 0, new Label(stringMessages.duration() + ":"));
        durationLabel = new Label();
        formGrid.setWidget(4, 1, durationLabel);
        mainPanel.add(formGrid);
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
        mediaTrack.url = urlBox.getValue();
        loadMediaDuration();

        String lastPathSegment = mediaTrack.url.substring(mediaTrack.url.lastIndexOf('/') + 1);
        int dotPos = lastPathSegment.lastIndexOf('.');
        if (dotPos >= 0) {
            mediaTrack.title = lastPathSegment.substring(0, dotPos);
            String fileEnding = lastPathSegment.substring(dotPos + 1).toLowerCase();
    
            try {
                mediaTrack.mimeType = MimeType.valueOf(fileEnding);
            } catch (IllegalArgumentException e) {
                // ignore. TODO: Somehow put it into the error message.
                // throw new IllegalArgumentException("Unsupported media type '" + mimeType + "'.", e);
                mediaTrack.mimeType = null;
            }
        } else {
            mediaTrack.title = mediaTrack.url;
            mediaTrack.mimeType = null;
        }
        refreshUI();
    }

    private void refreshUI() {
        titleBox.setValue(mediaTrack.title, DONT_FIRE_EVENTS);
        mimeTypeLabel.setText(mediaTrack.typeToString());
        String startTimeText = mediaTrack.startTime == null ? "undefined" : TimeFormatUtil.DATETIME_FORMAT.format(mediaTrack.startTime);
        startTimeLabel.setText(startTimeText);
        durationLabel.setText(TimeFormatUtil.milliSecondsToHrsMinSec(mediaTrack.durationInMillis));        
    }

    @Override
    public void show() {
        super.show();
        urlBox.setFocus(true);
    }
}
