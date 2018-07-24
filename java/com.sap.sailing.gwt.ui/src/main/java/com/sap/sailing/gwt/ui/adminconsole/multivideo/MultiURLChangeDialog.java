package com.sap.sailing.gwt.ui.adminconsole.multivideo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.TimeFormatUtil;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * This dialog allows to change multiple urls for mediatracks at once. It will determine the longest common prefix, that
 * all mediatracks share. This is very useful if a lot of videos are migrated to another server, eg. local hosted to s3
 * hosted.
 */
public class MultiURLChangeDialog extends DialogBox {
    private static final String EMPTY_TEXT = "";
    private static final int TITLE_COLUMN = 0;
    private static final int URL_COLUMN = 1;
    private static final int URL_COLUMN_NEW = 2;
    private static final int DURATION_COLUMN = 3;
    private static final int STARTTIME_COLUMN = 4;
    private static final int MIMETYPE_COLUMN = 5;

    private static final Style STYLE = GWT.<StyleHolder> create(StyleHolder.class).style();
    private StringMessages stringMessages;
    private List<MediaTrack> mediaTrackRenameMap = new ArrayList<>();
    private FlexTable dataTable;
    private Button doSaveButton;
    private TextBox replacePartIn;
    private TextBox replacePartOut;

    public MultiURLChangeDialog(MediaServiceAsync mediaService, StringMessages stringMessages, Set<MediaTrack> selected,
            ErrorReporter errorReporter, Runnable afterLinking) {
        this.stringMessages = stringMessages;
        setGlassEnabled(true);

        FlowPanel mainContent = new FlowPanel();
        // placeholder for description if required
        Label descriptionLabel = new Label(this.stringMessages.multiUrlChangeExplain());
        mainContent.add(descriptionLabel);
        
        mainContent.add(new Label(stringMessages.multiUrlChangeFind()));
        replacePartIn = new TextBox();
        replacePartIn.getElement().getStyle().setWidth(25, Unit.PCT);
        mainContent.add(replacePartIn);
        mainContent.add(new Label(stringMessages.multiUrlChangeReplace()));
        replacePartOut = new TextBox();
        replacePartOut.getElement().getStyle().setWidth(25, Unit.PCT);
        replacePartOut.setText("http://replace_me_with_baseurl/");
        mainContent.add(replacePartOut);

        replacePartOut.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateUI();
            }
        });
        replacePartOut.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateUI();
            }
        });
        replacePartOut.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateUI();
            }
        });

        FlowPanel buttonPanel = new FlowPanel();
        DockPanel dockPanel = new DockPanel();
        add(dockPanel);
        dockPanel.add(new ScrollPanel(mainContent), DockPanel.CENTER);

        dataTable = new FlexTable();
        STYLE.ensureInjected();
        dataTable.addStyleName(STYLE.tableStyle());
        mainContent.add(dataTable);

        Button cancelButton = new Button(stringMessages.close());
        cancelButton.getElement().getStyle().setMargin(3, Unit.PX);
        cancelButton.ensureDebugId("CancelButton");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MultiURLChangeDialog.this.hide();
            }
        });

        doSaveButton = new Button(stringMessages.multiUrlChangeSave());
        doSaveButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
                for (MediaTrack track : mediaTrackRenameMap) {
                    String newUrl = getPatchedUrl(track);
                    track.url = newUrl;
                    mediaService.updateUrl(track, new AsyncCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            afterLinking.run();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Notification.notify(stringMessages.multiUrlChangeCannotSave(), NotificationType.ERROR);
                        }
                    });
                }
            }
        });
        buttonPanel.add(doSaveButton);
        buttonPanel.add(cancelButton);

        dockPanel.add(buttonPanel, DockPanel.SOUTH);

        mediaTrackRenameMap = new ArrayList<>(selected);

        final String maxPrefixForAll = maxPrefixForAll();
        if (maxPrefixForAll.length() == 0) {
            Notification.notify(this.stringMessages.multiUrlNoPrefixWarning(), NotificationType.WARNING);
        }
        replacePartIn.setText(maxPrefixForAll);
        updateUI();
    }

    private String maxPrefixForAll() {
        List<String> allUrls = mediaTrackRenameMap.stream().map(f -> f.url).collect(Collectors.toList());

        int length = Integer.MAX_VALUE;
        for (String url : allUrls) {
            final int urlLength = url.length();
            if (urlLength < length) {
                length = urlLength;
            }
        }
        for (int i = 0; i < length; i++) {
            Character charTest = null;
            for (String url : allUrls) {
                char cur = url.charAt(i);
                if (charTest == null) {
                    charTest = cur;
                } else {
                    if (charTest.charValue() != cur) {
                        return url.substring(0, i);
                    }
                }
            }
        }
        // for some reason the whole urls do match
        return allUrls.get(0);
    }

    protected void updateUI() {
        int y = 0;
        dataTable.removeAllRows();
        dataTable.clear();
        dataTable.setWidget(y, TITLE_COLUMN, new Label(stringMessages.title()));
        dataTable.setWidget(y, URL_COLUMN, new Label(stringMessages.url()));
        dataTable.setWidget(y, URL_COLUMN_NEW, new Label(stringMessages.multiUrlChangeNewURL()));
        dataTable.setWidget(y, DURATION_COLUMN, new Label(stringMessages.duration()));
        dataTable.setWidget(y, STARTTIME_COLUMN, new Label(stringMessages.startTime()));
        dataTable.setWidget(y, MIMETYPE_COLUMN, new Label(stringMessages.mimeType()));

        for (int row = 0; row < dataTable.getCellCount(0); row++) {
            dataTable.getFlexCellFormatter().addStyleName(y, row, STYLE.tableHeader());
        }

        y++;
        for (MediaTrack remoteFile : mediaTrackRenameMap) {

            Label name = new Label(remoteFile.title);
            dataTable.setWidget(y, TITLE_COLUMN, name);

            Anchor link = new Anchor(remoteFile.url);
            link.setHref(remoteFile.url);
            link.setTarget("_blank");
            dataTable.setWidget(y, URL_COLUMN, link);

            String newUrl = getPatchedUrl(remoteFile);
            Anchor linknew = new Anchor(newUrl);
            linknew.setHref(newUrl);
            linknew.setTarget("_blank");
            dataTable.setWidget(y, URL_COLUMN_NEW, linknew);

            String durationtext = EMPTY_TEXT;
            if (remoteFile.duration != null) {
                durationtext = TimeFormatUtil.durationToHrsMinSec(remoteFile.duration);
            }
            dataTable.setWidget(y, DURATION_COLUMN, new Label(durationtext));
            String startTimeText = EMPTY_TEXT;
            if (remoteFile.startTime != null) {
                startTimeText = TimeFormatUtil.DATETIME_FORMAT.format(new Date(remoteFile.startTime.asMillis()));
            }
            dataTable.setWidget(y, STARTTIME_COLUMN, new Label(startTimeText));
            dataTable.setWidget(y, MIMETYPE_COLUMN, new Label(remoteFile.mimeType.name()));
            y++;
        }

        if (isShowing()) {
            center();
        }
    }

    private String getPatchedUrl(MediaTrack remoteFile) {
        String newUrl = remoteFile.url;
        newUrl = newUrl.substring(replacePartIn.getText().length());
        newUrl = replacePartOut.getText() + newUrl;
        return newUrl;
    }

    static interface StyleHolder extends ClientBundle {
        @Source("MultiVideoDialog.css")
        Style style();
    }

    static interface Style extends CssResource {
        String tableStyle();

        String tableHeader();

        String checkboxStyle();
    }
}
