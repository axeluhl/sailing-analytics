package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MediaSubType;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MediaType;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MimeFileType;

public class MediaTrackDialog extends DataEntryDialog<MediaTrack> {

    private static final boolean DONT_FIRE_EVENTS = false;

    private static final Validator<MediaTrack> MEDIA_TRACK_VALIDATOR = new Validator<MediaTrack>() {

        @Override
        public String getErrorMessage(MediaTrack valueToValidate) {
            return null;
        }

    };

    private final StringMessages stringMessages;

    private TextBox nameBox;

    private DateBox startTimeBox;

    private TextBox urlBox;

    private TextBox mimeTypeBox;

    public MediaTrackDialog(StringMessages stringMessages, AsyncCallback<MediaTrack> okCancelCallback) {
        super("Media Track", "", stringMessages.ok(), stringMessages.cancel(), MEDIA_TRACK_VALIDATOR, okCancelCallback);
        this.stringMessages = stringMessages;
    }

    @Override
    protected MediaTrack getResult() {
        MediaType mediaType;
        MediaSubType mediaSubType;
        try {
            String mimeType = mimeTypeBox.getValue();
            MimeFileType mimeFileType = MimeFileType.valueOf(mimeType);
            mediaType = mimeFileType.mediaType;
            mediaSubType = mimeFileType.mediaSubType;
        } catch (IllegalArgumentException e) {
            // ignore. TODO: Somehow put it into the error message.
            // throw new IllegalArgumentException("Unsupported media type '" + mimeType + "'.", e);
            mediaType = null;
            mediaSubType = null;
        }
        String title = nameBox.getValue();
        String url = urlBox.getValue();
        Date startTime = startTimeBox.getValue();
        MediaTrack result = new MediaTrack(title, url, startTime, mediaType, mediaSubType);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        Grid formGrid = new Grid(4, 2);
        formGrid.setCellSpacing(3);
        formGrid.setWidget(0, 0, new Label(stringMessages.name() + ":"));
        nameBox = createUrlBox();
        formGrid.setWidget(0, 1, nameBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.mimeType() + ":"));
        mimeTypeBox = createUrlBox();
        formGrid.setWidget(1, 1, mimeTypeBox);
        formGrid.setWidget(2, 0, new Label(stringMessages.url() + ":"));
        urlBox = createUrlBox();
        formGrid.setWidget(2, 1, urlBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.startTime() + ":"));
        startTimeBox = createDateBox(new Date().getTime(), 16);
        formGrid.setWidget(3, 1, startTimeBox);
        mainPanel.add(formGrid);
        return mainPanel;
    }

    private TextBox createUrlBox() {
        TextBox result = createTextBox(null);
        result.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                deriveNameAndMimeType();
            }

        });
        return result;
    }

    protected void deriveNameAndMimeType() {
        String url = urlBox.getValue();

        String lastPathSegment = url.substring(url.lastIndexOf('/') + 1);
        int dotPos = lastPathSegment.lastIndexOf('.');
        String fileEnding = lastPathSegment.substring(dotPos + 1);
        String fileName = lastPathSegment.substring(0, dotPos);

        nameBox.setValue(fileName, DONT_FIRE_EVENTS);
        mimeTypeBox.setValue(fileEnding, DONT_FIRE_EVENTS);
    }

    @Override
    public void show() {
        super.show();
        urlBox.setFocus(true);
    }
}
