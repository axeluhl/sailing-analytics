package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.EventDialog.FileStorageServiceConnectionTestObservable;
import com.sap.sailing.gwt.ui.adminconsole.EventDialog.FileStorageServiceConnectionTestObserver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.GWTLocaleUtil;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.media.VideoDTO;

public abstract class VideoDialog extends DataEntryDialog<VideoDTO> implements FileStorageServiceConnectionTestObserver {
    protected final StringMessages stringMessages;
    protected final URLFieldWithFileUpload videoURLAndUploadComposite;
    protected final Date creationDate;
    protected Label createdAtLabel;
    protected TextBox titleTextBox;
    protected TextBox subtitleTextBox;
    protected TextBox copyrightTextBox;
    protected final ListBox mimeTypeListBox;
    protected final ListBox localeListBox;
    protected IntegerBox lengthIntegerBox;
    protected final URLFieldWithFileUpload thumbnailURLAndUploadComposite;
    protected StringListInlineEditorComposite tagsListEditor;
    
    protected static class VideoParameterValidator implements Validator<VideoDTO> {
        private StringMessages stringMessages;

        public VideoParameterValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(VideoDTO videoToValidate) {
            String errorMessage = null;
            
            if(videoToValidate.getSourceRef() == null || videoToValidate.getSourceRef().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrl();
            } else if (videoToValidate.getMimeType() == null) {
                errorMessage = "You must select the mime type for the video.";
            }
            return errorMessage;
        }
    }

    public VideoDialog(Date createdAtDate, VideoParameterValidator validator, StringMessages stringMessages, FileStorageServiceConnectionTestObservable storageServiceAvailable, DialogCallback<VideoDTO> callback) {
        super(stringMessages.video(), null, stringMessages.ok(), stringMessages.cancel(), validator,
                callback);
        this.stringMessages = stringMessages;
        this.creationDate = createdAtDate;
        storageServiceAvailable.registerObserver(this);
        getDialogBox().getWidget().setWidth("730px");

        mimeTypeListBox = createListBox(false);
        mimeTypeListBox.addItem(MimeType.unknown.name());
        mimeTypeListBox.addItem(MimeType.aac.name());
        mimeTypeListBox.addItem(MimeType.mp4.name());
        mimeTypeListBox.addItem(MimeType.mp4panorama.name());
        mimeTypeListBox.addItem(MimeType.mp4panoramaflip.name());
        mimeTypeListBox.addItem(MimeType.ogg.name());
        mimeTypeListBox.addItem(MimeType.ogv.name());
        mimeTypeListBox.addItem(MimeType.qt.name());
        mimeTypeListBox.addItem(MimeType.youtube.name());
        mimeTypeListBox.addItem(MimeType.vimeo.name());
        localeListBox = createListBox(false);
        for (String locale : GWTLocaleUtil.getAvailableLocalesAndDefault()) {
            localeListBox.addItem(GWTLocaleUtil.getDecoratedLanguageDisplayNameWithDefaultLocaleSupport(locale), locale == null ? "" : locale);
        }
        videoURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages, false);
        videoURLAndUploadComposite.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validateAndUpdate();
            }
        });
        thumbnailURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages, false);
        tagsListEditor = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new GenericStringListInlineEditorComposite.ExpandedUi<String>(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        MediaTagConstants.videoTagSuggestions, stringMessages.enterTagsForTheVideo(), 50));
    }

    @Override
    protected VideoDTO getResult() {
        VideoDTO result = new VideoDTO(videoURLAndUploadComposite.getURL(), getSelectedMimeType(), creationDate);
        result.setTitle(titleTextBox.getValue());
        result.setSubtitle(subtitleTextBox.getValue());
        result.setCopyright(copyrightTextBox.getValue());
        result.setLocale(getSelectedLocale());
        List<String> tags = new ArrayList<String>();
        for (String tag: tagsListEditor.getValue()) {
            tags.add(tag);
        }
        result.setTags(tags);
        result.setThumbnailRef(thumbnailURLAndUploadComposite.getURL());
        return result;
    }

    protected void setSelectedMimeType(MimeType mimeType) {
        for(int i = 0; i < mimeTypeListBox.getItemCount(); i++) {
            if(mimeTypeListBox.getItemText(i).equals(mimeType.name())) {
                mimeTypeListBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private MimeType getSelectedMimeType() {
        MimeType result = null;
        int selectedIndex = mimeTypeListBox.getSelectedIndex();
        if(selectedIndex >= 0) {
            result = MimeType.valueOf(mimeTypeListBox.getSelectedValue());
        }
        return result;
    }
    
    protected void setSelectedLocale(String locale) {
        for(int i = 0; i < localeListBox.getItemCount(); i++) {
            if(Util.equalsWithNull(localeListBox.getValue(i), locale)) {
                localeListBox.setSelectedIndex(i);
                return;
            }
        }
        localeListBox.setSelectedIndex(0);
    }
    
    private String getSelectedLocale() {
        return localeListBox.getSelectedValue();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        Grid formGrid = new Grid(10, 2);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label("Created at:"));
        formGrid.setWidget(0, 1, createdAtLabel);
        formGrid.setWidget(1,  0, new Label("Video URL:"));
        formGrid.setWidget(1, 1, videoURLAndUploadComposite);
        formGrid.setWidget(2,  0, new Label("Mime Type:"));
        formGrid.setWidget(2, 1, mimeTypeListBox);
        
        
        formGrid.setWidget(3,  0, new Label(stringMessages.title() + ":"));
        formGrid.setWidget(3, 1, titleTextBox);
        formGrid.setWidget(4,  0, new Label("Subtitle:"));
        formGrid.setWidget(4, 1, subtitleTextBox);
        formGrid.setWidget(5, 0, new Label("Copyright:"));
        formGrid.setWidget(5, 1, copyrightTextBox);
        formGrid.setWidget(6, 0, new Label("Language:"));
        formGrid.setWidget(6, 1, localeListBox);
        formGrid.setWidget(7, 0, new Label("Tags:"));
        formGrid.setWidget(7, 1, tagsListEditor);

        formGrid.setWidget(8, 0, new Label("Video-Length (s):"));
        formGrid.setWidget(8, 1, lengthIntegerBox);
        formGrid.setWidget(9, 0, new Label("Thumbnail-URL:"));
        formGrid.setWidget(9, 1, thumbnailURLAndUploadComposite);

        return panel;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return videoURLAndUploadComposite.getInitialFocusWidget();
    }
    
    @Override
    public void onFileStorageServiceTestPassed() {
        videoURLAndUploadComposite.setUploadEnabled(true);
        thumbnailURLAndUploadComposite.setUploadEnabled(true);
    }
}
