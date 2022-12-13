package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.gwt.ui.adminconsole.FileStorageServiceConnectionTestObservable.FileStorageServiceConnectionTestObserver;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.JSDownloadUtils;
import com.sap.sailing.gwt.ui.client.media.JSDownloadUtils.JSDownloadCallback;
import com.sap.sse.common.Util;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.GWTLocaleUtil;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.media.VideoDTO;

public abstract class VideoDialog extends DataEntryDialog<List<VideoDTO>> implements FileStorageServiceConnectionTestObserver {
    protected final StringMessages stringMessages;
    protected final URLFieldWithFileUpload videoURLAndUploadComposite;
    protected final Date creationDate;
    protected Label createdAtLabel;
    protected final VerticalPanel fileInfoVPanel;
    protected final VerticalPanel fileSingleInfoVPanel;
    protected TextBox subtitleTextBox;
    protected TextBox copyrightTextBox;
    protected final ListBox localeListBox;
    protected final URLFieldWithFileUpload thumbnailURLAndUploadComposite;
    protected StringListInlineEditorComposite tagsListEditor;
    private final MediaServiceAsync mediaService;
    private final List<VideoTmpData> videoTmpDatas;
    
    private static class VideoTmpData {
        String uri;
        String fileName;
        String title;
        Integer lengthInSeconds;
        MimeType mimeType;
    }

    protected static class VideoParameterValidator implements Validator<List<VideoDTO>> {
        private StringMessages stringMessages;

        public VideoParameterValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(List<VideoDTO> videosToValidate) {
            StringJoiner errorJoiner = new StringJoiner("\n");
            for (VideoDTO videoToValidate : videosToValidate) {
                String errorMessage = null;

                if(videoToValidate.getSourceRef() == null || videoToValidate.getSourceRef().isEmpty()) {
                    errorMessage = stringMessages.pleaseEnterNonEmptyUrl();
                } else if (videoToValidate.getMimeType() == null) {
                    errorMessage = "You must select the mime type for the video.";
                }
                if (errorMessage != null) {
                    errorJoiner.add(errorMessage);
                }
            }
            return errorJoiner.toString();
        }
    }
    
    private void updateMetaInfo(String url, Label lbl, AsyncCallback<VideoMetadataDTO> asyncCallback) {
        mediaService.checkMetadata(url, new AsyncCallback<VideoMetadataDTO>() {
            @Override
            public void onSuccess(VideoMetadataDTO result) {
                GWT.log("Success getting meta data from service: " + result.getMessage());
                //remoteMp4WasFinished = true;
                if (result.isDownloadable() && result.getDuration() != null) {
                    GWT.log("downloadable. Duration: " + result.getDuration());
                    asyncCallback.onSuccess(result);
                } else {
                    checkMetadataOnClient(url, lbl, asyncCallback);
                }
            }
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error in backend", caught);
                // try on client instead
                checkMetadataOnClient(url, lbl, asyncCallback);
            }
            private void checkMetadataOnClient(String url, Label lbl, AsyncCallback<VideoMetadataDTO> asyncCallback) {
                GWT.log("checkMetadataOnClient, url: " + url);
                JSDownloadUtils.getData(url, new JSDownloadCallback() {
                    @Override
                    public void progress(Double current, Double total) {
                        GWT.log("progress");
                        lbl.setText(stringMessages.transferStarted() + " " + Math.round(current / 1024 / 1024) + "/"
                                + Math.round(total / 1024 / 1024) + " MB");
                    }
                    @Override
                    public void error(Object msg) {
                        GWT.log("error");
                        asyncCallback
                                .onSuccess(new VideoMetadataDTO(false, null, false, null, msg == null ? "" : msg.toString()));
                    }
                    @Override
                    public void complete(Int8Array start, Int8Array end, Double skipped) {
                        GWT.log("complete");
                        lbl.setText(stringMessages.analyze());
                        //infoLabel.setWidget(lbl);
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

    protected void initVideoTmpDatas(String uri, String title, Integer lengthInSeconds, MimeType mimeType) {
        VideoTmpData videoTmpData = new VideoTmpData();
        videoTmpDatas.add(videoTmpData);
        videoTmpData.uri = uri;
        videoTmpData.fileName = extractFileName(uri);
        videoTmpData.title = title;
        videoTmpData.lengthInSeconds = lengthInSeconds;
        videoTmpData.mimeType = mimeType;
        renderMultiFileTable(false);
    }
    
    private String extractFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public VideoDialog(Date createdAtDate, VideoParameterValidator validator, StringMessages stringMessages, FileStorageServiceConnectionTestObservable storageServiceAvailable, DialogCallback<List<VideoDTO>> callback, MediaServiceAsync mediaService) {
        super(stringMessages.video(), null, stringMessages.ok(), stringMessages.cancel(), validator,
                callback);
        this.stringMessages = stringMessages;
        this.creationDate = createdAtDate;
        this.mediaService = mediaService;
        videoTmpDatas = new ArrayList<VideoDialog.VideoTmpData>();
        getDialogBox().getWidget().setWidth("730px");
        fileInfoVPanel = new VerticalPanel();
        fileSingleInfoVPanel = new VerticalPanel();
        localeListBox = createListBox(false);
        for (String locale : GWTLocaleUtil.getAvailableLocalesAndDefault()) {
            localeListBox.addItem(GWTLocaleUtil.getDecoratedLanguageDisplayNameWithDefaultLocaleSupport(locale), locale == null ? "" : locale);
        }
        videoURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages, true, true, true, "audio/*,video/*");
        videoURLAndUploadComposite.addValueChangeHandler(new ValueChangeHandler<Map<String, String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Map<String, String>> event) {
                validateAndUpdate();
                videoTmpDatas.clear();
                for (Entry<String, String> upload: videoURLAndUploadComposite.getValue().entrySet()) {
                    VideoTmpData videoTmpData = new VideoTmpData();
                    videoTmpDatas.add(videoTmpData);
                    videoTmpData.uri = upload.getKey();
                    videoTmpData.fileName = upload.getValue();
                    videoTmpData.mimeType = extractMimeTypeByFilename(videoTmpData.fileName);
                    final String title;
                    if (videoTmpData.fileName.contains(".")) {
                        title = videoTmpData.fileName.substring(0, videoTmpData.fileName.lastIndexOf('.'));
                    } else {
                        title = videoTmpData.fileName;
                    }
                    videoTmpData.title = title;
                }
                renderMultiFileTable(true);
                
//                Grid videoGrid = new Grid(uploads.size() + 1, 4);
//                fileInfoVPanel.clear();
//                fileInfoVPanel.add(videoGrid);
//                // add header
//                videoGrid.setWidget(0, 0, new Label(stringMessages.name()));
//                videoGrid.setWidget(0, 1, new Label(stringMessages.title()));
//                videoGrid.setWidget(0, 2, new Label(stringMessages.mimeType()));
//                videoGrid.setWidget(0, 3, new Label(stringMessages.videoLengthSeconds()));
//                // process single upload items
//                for (int i = 0; i < uploads.size(); i++) {
//                    String videoUri = uploads.get(i).getKey();
//                    VideoTmpData videoTmpData = new VideoTmpData();
//                    videoTmpDatas.add(videoTmpData);
//                    videoTmpData.uri = videoUri;
//                    final String fileNameWithExtension = uploads.get(i).getValue();
//                    final String fileName;
//                    if (fileNameWithExtension.contains(".")) {
//                        fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));
//                    } else {
//                        fileName = fileNameWithExtension;
//                    }
//                    TextBox titleTextBox = new TextBox();
//                    titleTextBox.setText(fileName);
//                    videoTmpData.title = fileName;
//                    titleTextBox.addChangeHandler(new ChangeHandler() {
//                        @Override
//                        public void onChange(ChangeEvent event) {
//                            videoTmpData.title = titleTextBox.getValue();
//                        }
//                    });
//                    Label nameLabel = new Label(stringMessages.transferStarted());
//                    MimeType mimeType = extractMimeTypeByFilename(fileName);
//                    ListBox mimeTypeListBox = createMimeTextSelection();
//                    mimeTypeListBox.addChangeHandler(new ChangeHandler() {
//                        @Override
//                        public void onChange(ChangeEvent event) {
//                            videoTmpData.mimeType = getSelectedMimeType(mimeTypeListBox);
//                        }
//                    });
//                    setSelectedMimeType(mimeTypeListBox, mimeType);
//                    videoTmpData.mimeType = mimeType;
//                    Label durationLabel = new Label();
//                    videoGrid.setWidget(i + 1, 0, nameLabel);
//                    videoGrid.setWidget(i + 1, 1, titleTextBox);
//                    videoGrid.setWidget(i + 1, 2, mimeTypeListBox);
//                    videoGrid.setWidget(i + 1, 3, durationLabel);
//                    updateMetaInfo(videoUri, nameLabel, new AsyncCallback<VideoMetadataDTO>() {
//                        @Override
//                        public void onSuccess(VideoMetadataDTO result) {
//                            nameLabel.setText(fileNameWithExtension);
//                            if (result != null && result.getDuration() != null) {
//                                durationLabel.setText(Double.toString(result.getDuration().asSeconds()));
//                                videoTmpData.lengthInSeconds = (int) result.getDuration().asSeconds();
//
//                            } else {
//                                durationLabel.setText("");
//                            }
//                        }
//                        @Override
//                        public void onFailure(Throwable caught) {
//                            GWT.log("Error", caught);
//                            nameLabel.setText(fileNameWithExtension);
//                        }
//                    });
//                }
            }
        });
        thumbnailURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages, false, true, true, "image/*");
        tagsListEditor = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new GenericStringListInlineEditorComposite.ExpandedUi<String>(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        MediaTagConstants.videoTagSuggestions, stringMessages.enterTagsForTheVideo(), 50));
        //the observer has to be registered after creating the URLFieldWithFileUpload
        storageServiceAvailable.registerObserver(this);
    }
    
    private void renderMultiFileTable(boolean updateMediaInfo) {
        fileSingleInfoVPanel.clear();
        fileInfoVPanel.clear();
        if (videoTmpDatas.size() == 1) {
            Grid videoGrid = new Grid(3, 2);
            fileSingleInfoVPanel.add(videoGrid);
            VideoTmpData videoTmpData = videoTmpDatas.get(0);
            String videoUri = videoTmpData.uri;
            TextBox titleTextBox = new TextBox();
            titleTextBox.setText(videoTmpData.title);
            titleTextBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    videoTmpData.title = titleTextBox.getValue();
                }
            });
            ListBox mimeTypeListBox = createMimeTextSelection();
            mimeTypeListBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    videoTmpData.mimeType = getSelectedMimeType(mimeTypeListBox);
                }
            });
            setSelectedMimeType(mimeTypeListBox, videoTmpData.mimeType);
            String seconds = videoTmpData.lengthInSeconds != null ? videoTmpData.lengthInSeconds.toString() : "";
            Label durationLabel = new Label(seconds);
            videoGrid.setWidget(0, 0, new Label(stringMessages.title() + ":"));
            videoGrid.setWidget(0, 1, titleTextBox);
            videoGrid.setWidget(1, 0, new Label(stringMessages.mimeType() + ":"));
            videoGrid.setWidget(1, 1, mimeTypeListBox);
            videoGrid.setWidget(2, 0, new Label(stringMessages.videoLengthSeconds() + ":"));
            videoGrid.setWidget(2, 1, durationLabel);
            if (updateMediaInfo) {
                Label statusLabel = new Label();
                fileInfoVPanel.add(statusLabel);
                updateMetaInfo(videoUri, statusLabel, new AsyncCallback<VideoMetadataDTO>() {
                    @Override
                    public void onSuccess(VideoMetadataDTO result) {
                        if (result != null && result.getDuration() != null) {
                            durationLabel.setText(Double.toString(result.getDuration().asSeconds()));
                            videoTmpData.lengthInSeconds = (int) result.getDuration().asSeconds();

                        } else {
                            durationLabel.setText("");
                            videoTmpData.lengthInSeconds = null;
                        }
                        statusLabel.setText("");
                        statusLabel.setVisible(false);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error", caught);
                        statusLabel.setText("");
                        statusLabel.setVisible(false);
                    }
                });
            }
        } else {
            Grid videoGrid = new Grid(videoTmpDatas.size() + 1, 4);
            fileInfoVPanel.add(videoGrid);
            // add header
            videoGrid.setWidget(0, 0, new Label(stringMessages.name()));
            videoGrid.setWidget(0, 1, new Label(stringMessages.title()));
            videoGrid.setWidget(0, 2, new Label(stringMessages.mimeType()));
            videoGrid.setWidget(0, 3, new Label(stringMessages.videoLengthSeconds()));
            // process single upload items
            for (int i = 0; i < videoTmpDatas.size(); i++) {
                VideoTmpData videoTmpData = videoTmpDatas.get(i);
                String videoUri = videoTmpData.uri;
                TextBox titleTextBox = new TextBox();
                titleTextBox.setText(videoTmpData.title);
                titleTextBox.addChangeHandler(new ChangeHandler() {
                    @Override
                    public void onChange(ChangeEvent event) {
                        videoTmpData.title = titleTextBox.getValue();
                    }
                });
                Label nameLabel = new Label(videoTmpData.fileName);
                ListBox mimeTypeListBox = createMimeTextSelection();
                mimeTypeListBox.addChangeHandler(new ChangeHandler() {
                    @Override
                    public void onChange(ChangeEvent event) {
                        videoTmpData.mimeType = getSelectedMimeType(mimeTypeListBox);
                    }
                });
                setSelectedMimeType(mimeTypeListBox, videoTmpData.mimeType);
                String seconds = videoTmpData.lengthInSeconds != null ? videoTmpData.lengthInSeconds.toString() : "";
                Label durationLabel = new Label(seconds);
                videoGrid.setWidget(i + 1, 0, nameLabel);
                videoGrid.setWidget(i + 1, 1, titleTextBox);
                videoGrid.setWidget(i + 1, 2, mimeTypeListBox);
                videoGrid.setWidget(i + 1, 3, durationLabel);
                if (updateMediaInfo) {
                    nameLabel.setText(stringMessages.transferStarted());
                    updateMetaInfo(videoUri, nameLabel, new AsyncCallback<VideoMetadataDTO>() {
                        @Override
                        public void onSuccess(VideoMetadataDTO result) {
                            nameLabel.setText(videoTmpData.fileName);
                            if (result != null && result.getDuration() != null) {
                                durationLabel.setText(Double.toString(result.getDuration().asSeconds()));
                                videoTmpData.lengthInSeconds = (int) result.getDuration().asSeconds();
    
                            } else {
                                durationLabel.setText("");
                                videoTmpData.lengthInSeconds = null;
                            }
                            nameLabel.setText(videoTmpData.fileName);
                        }
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error", caught);
                            nameLabel.setText(videoTmpData.fileName);
                        }
                    });
                }
            }
        }
    }

    private MimeType extractMimeTypeByFilename(String fileName) {
        final MimeType result;
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos >= 0) {
            String fileEnding = fileName.substring(dotPos + 1).toLowerCase();
            List<MimeType> possibleMimeTypes = MimeType.byExtension(fileEnding);
            if (possibleMimeTypes.size() > 0) {
                result = possibleMimeTypes.get(0);
            } else {
                result = MimeType.unknown;
            }
        } else {
            result = MimeType.unknown;
        }
        return result;
    }

    @Override
    protected List<VideoDTO> getResult() {
        List<VideoDTO> results = new ArrayList<VideoDTO>(videoURLAndUploadComposite.getUris().size());
        for (VideoTmpData videoData : videoTmpDatas) {
            VideoDTO videoDTO = new VideoDTO(videoData.uri, videoData.mimeType, creationDate);
            videoDTO.setTitle(videoData.title);
            videoDTO.setSubtitle(subtitleTextBox.getValue());
            videoDTO.setCopyright(copyrightTextBox.getValue());
            videoDTO.setLocale(getSelectedLocale());
            List<String> tags = new ArrayList<String>();
            for (String tag: tagsListEditor.getValue()) {
                tags.add(tag);
            }
            videoDTO.setTags(tags);
            videoDTO.setThumbnailRef(thumbnailURLAndUploadComposite.getUri());
            videoDTO.setLengthInSeconds(videoData.lengthInSeconds);
            results.add(videoDTO);
        }
        return results;
    }
    
    private ListBox createMimeTextSelection() {
        ListBox mimeTypeListBox = createListBox(false);
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
        return mimeTypeListBox;
    }

    protected void setSelectedMimeType(ListBox mimeTypeListBox,MimeType mimeType) {
        for(int i = 0; i < mimeTypeListBox.getItemCount(); i++) {
            if(mimeTypeListBox.getItemText(i).equals(mimeType.name())) {
                mimeTypeListBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private MimeType getSelectedMimeType(ListBox mimeTypeListBox) {
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
        Grid formGrid = new Grid(9, 2);
        panel.add(formGrid);
        formGrid.setWidget(0, 0, new Label(stringMessages.createdAt() + ":"));
        formGrid.setWidget(0, 1, createdAtLabel);
        formGrid.setWidget(1,  0, new Label(stringMessages.videoUrl() + ":"));
        formGrid.setWidget(1, 1, videoURLAndUploadComposite);
        formGrid.setWidget(2, 1, fileInfoVPanel);
        formGrid.setWidget(3, 0, fileSingleInfoVPanel);
        formGrid.setWidget(4,  0, new Label(stringMessages.subtitle() + ":"));
        formGrid.setWidget(4, 1, subtitleTextBox);
        formGrid.setWidget(5, 0, new Label(stringMessages.copyright() + ":"));
        formGrid.setWidget(5, 1, copyrightTextBox);
        formGrid.setWidget(6, 0, new Label(stringMessages.language() + ":"));
        formGrid.setWidget(6, 1, localeListBox);
        formGrid.setWidget(7, 0, new Label(stringMessages.tags() + ":"));
        formGrid.setWidget(7, 1, tagsListEditor);
        formGrid.setWidget(8, 0, new Label(stringMessages.thumbnailUrl() + ":"));
        formGrid.setWidget(8, 1, thumbnailURLAndUploadComposite);
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
