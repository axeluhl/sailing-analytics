package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.TimeFormatUtil;
import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack.MimeType;

public class MediaTrackYoutubeDialog extends DataEntryDialog<MediaTrack> {

    private static final boolean DONT_FIRE_EVENTS = false;

    private static final Validator<MediaTrack> MEDIA_TRACK_VALIDATOR = new Validator<MediaTrack>() {

        @Override
        public String getErrorMessage(MediaTrack valueToValidate) {
            return null;
        }

    };

    private final StringMessages stringMessages;
    
    private MediaTrack mediaTrack = new MediaTrack();

    private TextBox youtubeIdBox;
    
    private Label titleLabel;

    private Label durationLabel;

    private TextArea descriptionText;
    
    private SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

    public MediaTrackYoutubeDialog(StringMessages stringMessages, DialogCallback<MediaTrack> dialogCallback) {
        super(stringMessages.addYoutubeTrack(), "", stringMessages.ok(), stringMessages.cancel(), MEDIA_TRACK_VALIDATOR, dialogCallback);
        this.stringMessages = stringMessages;
        registerNativeMethods();
    }

    @Override
    protected MediaTrack getResult() {
        mediaTrack.url = youtubeIdBox.getValue();
        mediaTrack.mimeType = MimeType.youtube;
        return mediaTrack;
    }
    
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(5, 2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, new Label(stringMessages.youtubeId() + ":"));
        youtubeIdBox = createYoutubeIdBox();
        formGrid.setWidget(0, 1, youtubeIdBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.name() + ":"));
        titleLabel = new Label();
        formGrid.setWidget(1, 1, titleLabel);
        formGrid.setWidget(2, 0, new Label(stringMessages.duration() + ":"));
        durationLabel = new Label();
        formGrid.setWidget(2, 1, durationLabel);
        mainPanel.add(formGrid);
        descriptionText = new TextArea();
        mainPanel.add(descriptionText);
        return mainPanel;
    }

    private TextBox createYoutubeIdBox() {
        TextBox result = createTextBox(null);
        result.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                startUpdateMetadata();
                setUiEnabled(false);
            }

        });
        return result;
    }

    protected void setUiEnabled(boolean isEnabled) {
        youtubeIdBox.setEnabled(isEnabled);
        getOkButton().setEnabled(isEnabled);
        if (isEnabled)  {
            setCursor(Style.Cursor.AUTO);
        } else {
            setCursor(Style.Cursor.WAIT);
        }
    }

    protected void startUpdateMetadata() {
        mediaTrack.url = youtubeIdBox.getValue();
        loadYoutubeMetadata(mediaTrack.url);
    }

    private native void registerNativeMethods() /*-{
                var that = this;
		window.youtubeMetadataCallback = function(metadata) {
		        var title = metadata.entry.media$group.media$title.$t;
		        var duration = metadata.entry.media$group.yt$duration.seconds;
		        var description = metadata.entry.media$group.media$description.$t;
			that.@com.sap.sailing.gwt.ui.adminconsole.MediaTrackYoutubeDialog::youtubeMetadataCallback(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(title, duration, description);
		}
    }-*/;

    /** Inspired by https://developers.google.com/web-toolkit/doc/latest/tutorial/Xsite
     * 
     * @param youtubeId
     */
    public native void loadYoutubeMetadata(String youtubeId) /*-{
                var that = this;

		//Create temporary script element.
		window.youtubeMetadataCallbackScript = document.createElement("script");
		window.youtubeMetadataCallbackScript.src = "http://gdata.youtube.com/feeds/api/videos/" + youtubeId + "?alt=json&orderby=published&format=6&callback=youtubeMetadataCallback";
		document.body.appendChild(window.youtubeMetadataCallbackScript);

                // Cancel meta data capturing after has 2-seconds timeout.
                setTimeout(function() {
                  //Remove temporary script element.
                  document.body.removeChild(window.youtubeMetadataCallbackScript);
                  delete window.youtubeMetadataCallbackScript;
                  that.@com.sap.sailing.gwt.ui.adminconsole.MediaTrackYoutubeDialog::setUiEnabled(Z)(true);
                }, 2000);
             
    }-*/;

    public void youtubeMetadataCallback(String title, String durationInSeconds, String description) {
        setUiEnabled(true);
        mediaTrack.title = title;
        try {
            mediaTrack.durationInMillis = (int) (1000 * Double.valueOf(durationInSeconds));
        } catch (NumberFormatException ex) {
            mediaTrack.durationInMillis = 0;
        }
        descriptionText.setText(description);
        refreshUI();
    }

    private void refreshUI() {
        titleLabel.setText(mediaTrack.title);
        durationLabel.setText(TimeFormatUtil.milliSecondsToHrsMinSec(mediaTrack.durationInMillis));        
    }

    @Override
    public void show() {
        super.show();
        youtubeIdBox.setFocus(true);
    }
}
