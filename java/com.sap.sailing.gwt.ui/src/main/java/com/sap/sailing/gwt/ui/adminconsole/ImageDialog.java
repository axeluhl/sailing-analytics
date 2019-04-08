package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.EventDialog.FileStorageServiceConnectionTestObservable;
import com.sap.sailing.gwt.ui.adminconsole.EventDialog.FileStorageServiceConnectionTestObserver;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.listedit.ExpandedUiWithCheckboxes;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;

public abstract class ImageDialog extends DataEntryDialog<ImageResizingTaskDTO>
        implements FileStorageServiceConnectionTestObserver {
    private final SailingServiceAsync sailingService;
    
    protected final StringMessages stringMessages;
    protected final URLFieldWithFileUpload imageURLAndUploadComposite;
    protected final Date creationDate;
    protected Label createdAtLabel;
    protected TextBox titleTextBox;
    protected TextBox subtitleTextBox;
    protected TextBox copyrightTextBox;
    protected IntegerBox widthInPxBox;
    protected IntegerBox heightInPxBox;
    protected final StringListInlineEditorComposite tagsListEditor;
    protected Image image;
    private final ExpandedUiWithCheckboxes<String> expandedUi;
    private final BusyIndicator busyIndicator;

    protected static class ImageParameterValidator implements Validator<ImageResizingTaskDTO> {
        private final StringMessages stringMessages;
        private List<CheckBox> doResize;
        private final FileStorageServiceConnectionTestObservable storageServiceAvailable;

        public ImageParameterValidator(StringMessages stringMessages,
                FileStorageServiceConnectionTestObservable storageServiceAvailable) {
            this.stringMessages = stringMessages;
            this.storageServiceAvailable = storageServiceAvailable;
            this.doResize = new ArrayList<CheckBox>();
        }
        
        public void setCheckBoxes(List<CheckBox> doResize) {
            this.doResize = doResize;
        }

        /*
         * author Robin Fleige (D067799)
         * will return an error message if the image is too small
         * will return an error message if the width-height-ratio is not fitting for a resize
         * will return an error message and show a checkbox to allow resizing if the image is too big
         * will disable the checkbox if there is no working FileStorageService
         */
        @Override
        public String getErrorMessage(final ImageResizingTaskDTO resizingTask) {
            String errorMessage = null;
            final ImageDTO imageToValidate = resizingTask.getImage();
            final Integer imageWidth = imageToValidate.getWidthInPx();
            final Integer imageHeight = imageToValidate.getHeightInPx();

            if (imageToValidate.getSourceRef() == null || imageToValidate.getSourceRef().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrlOrUploadImage();
            } else if (imageWidth == null || imageHeight == null) {
                errorMessage = stringMessages.couldNotRetrieveImageSizeYet();
            } else {
                // check if image is too small for resizing
                errorMessage = "";
                for (MediaTagConstants mediaTag : MediaTagConstants.values()) {
                    if (imageToValidate.hasTag(mediaTag.getName())
                            && (imageWidth < mediaTag.getMinWidth() || imageHeight < mediaTag.getMinHeight())) {
                        errorMessage += getImageToSmallErrorMessage(mediaTag, stringMessages) + "\n";
                    }
                }
                if (errorMessage.equals("")) {// Check if image ratio fits for resizing
                    errorMessage = imageRatioFits(imageToValidate);
                }
                if (errorMessage.equals("")) {// check for checkboxes and resizing
                    for (MediaTagConstants mediaTag : MediaTagConstants.values()) {
                        final CheckBox checkBox = getCheckBoxForTag(mediaTag.getName(), imageToValidate);
                        if (imageToValidate.hasTag(mediaTag.getName())
                                && (imageWidth > mediaTag.getMaxWidth() || imageHeight > mediaTag.getMaxHeight())) {
                            if (!resizingTask.getResizingTask().contains(mediaTag)) {
                                errorMessage += getSizeErrorMessage(mediaTag, stringMessages) + "\n";
                                checkBox.setStyleName(ExpandedUiWithCheckboxes.getErrorStyle());
                                if (!errorMessage.equals("") && !storageServiceAvailable.getValue()) {
                                    checkBox.setEnabled(false);
                                }
                            } else {
                                checkBox.setStyleName(ExpandedUiWithCheckboxes.getNormalStyle());
                            }
                        } else {
                            checkBox.setStyleName(ExpandedUiWithCheckboxes.getInvisibleStyle());
                            checkBox.setValue(false);
                        }
                    }
                }
                if (!errorMessage.equals("") && !storageServiceAvailable.getValue()) {
                    errorMessage += stringMessages.automaticResizeNeedsStorageService() + "\n";
                }
            }
            if (errorMessage.equals("")) {
                errorMessage = null;
            }
            return errorMessage;
        }

        /**
         * Searches for the checkbox for the given tag and returns it
         * 
         * @author Robin Fleige (D067799)
         * 
         * @param tag
         *            the tag, the checkbox for is needed
         * @param imageToValidate
         *            the image with a list of all tags
         * @returns the fitting checkbox for the tag
         */
        private CheckBox getCheckBoxForTag(final String tag, final ImageDTO imageToValidate) {
            final List<String> tags = imageToValidate.getTags();
            CheckBox toReturn = null;
            for (int i = 0; i < tags.size(); i++) {
                if (tags.get(i).equals(tag)) {
                    toReturn = doResize.get(i);
                }
            }
            return toReturn == null ? new CheckBox() : toReturn;
            // new checkbox instead of null, so there is no need for a null check. this will be deleted from garbage
            // collector anyway
        }
        
        /**
         * Calculates if
         * 
         * @author Robin Fleige (D067799)
         * 
         * @param imageToValidate
         *            the image which has to be validated
         * @returns true if the imageRatio fits into the bounds of all its {@link MediaTagConstants}
         */
        private String imageRatioFits(ImageDTO imageToValidate) {
            String errorMessage = "";
            final double ratio = ((double) imageToValidate.getWidthInPx()) / imageToValidate.getHeightInPx();
            for (MediaTagConstants mediaTag : MediaTagConstants.values()) {
                if (imageToValidate.hasTag(mediaTag.getName())) {
                    final double minRatio = ((double) mediaTag.getMaxWidth()) / mediaTag.getMinHeight();
                    final double maxRatio = ((double) mediaTag.getMinWidth()) / mediaTag.getMaxHeight();
                    if (minRatio < ratio || maxRatio > ratio) {
                        errorMessage += stringMessages.imageResizeError(mediaTag.getName(), minRatio, maxRatio, ratio);
                    }
                }
            }
            return errorMessage;
        }
        
        private String getSizeErrorMessage(MediaTagConstants mediaTag, StringMessages stringMessages) {
            String errorMessage = stringMessages.imageSizeError(mediaTag.getName(), mediaTag.getMinWidth(),
                    mediaTag.getMaxWidth(), mediaTag.getMinHeight(), mediaTag.getMaxHeight());
            return errorMessage;
        }
        
        private String getImageToSmallErrorMessage(MediaTagConstants mediaTag, StringMessages stringMessages) {
            String errorMessage = stringMessages.imageToSmallError(mediaTag.getName(), mediaTag.getMinWidth(),
                    mediaTag.getMinHeight());
            return errorMessage;
        }
    }

    public ImageDialog(Date creationDate, SailingServiceAsync sailingService, StringMessages stringMessages,
            FileStorageServiceConnectionTestObservable storageServiceAvailable,
            DialogCallback<ImageResizingTaskDTO> callback) {
        this(creationDate, sailingService, stringMessages, storageServiceAvailable,
                new ImageParameterValidator(stringMessages, storageServiceAvailable), callback);
    }

    private ImageDialog(Date creationDate, SailingServiceAsync sailingService, StringMessages stringMessages,
            FileStorageServiceConnectionTestObservable storageServiceAvailable, ImageParameterValidator validator,
            DialogCallback<ImageResizingTaskDTO> callback) {
        super(stringMessages.image(), null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.creationDate = creationDate;
        getDialogBox().getWidget().setWidth("730px");
        busyIndicator = new SimpleBusyIndicator();
        imageURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages, false);
        imageURLAndUploadComposite.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String imageUrlAsString = event.getValue();
                if (imageUrlAsString == null || imageUrlAsString.isEmpty()) {
                    widthInPxBox.setText("");
                    heightInPxBox.setText("");
                } else {
                    busyIndicator.setBusy(true);
                    ImageDialog.this.sailingService.resolveImageDimensions(imageUrlAsString,
                            new AsyncCallback<Util.Pair<Integer, Integer>>() {
                                @Override
                                public void onSuccess(Pair<Integer, Integer> imageSize) {
                                    busyIndicator.setBusy(false);
                                    if (imageSize != null) {
                                        widthInPxBox.setValue(imageSize.getA());
                                        heightInPxBox.setValue(imageSize.getB());
                                    }
                                    validateAndUpdate();
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    busyIndicator.setBusy(false);
                                }
                            });
                }
                validateAndUpdate();
            }
        });
        //the observer has to be registered after creating the URLFieldWithFileUpload
        storageServiceAvailable.registerObserver(this);
        expandedUi = new ExpandedUiWithCheckboxes<String>(stringMessages, IconResources.INSTANCE.removeIcon(),
                /* suggestValues */ MediaTagConstants.imageTagSuggestions, stringMessages.enterTagsForTheImage(), 30,
                stringMessages.allowResizing());
        expandedUi.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validateAndUpdate();
            }
        });
        validator.setCheckBoxes(expandedUi.getCheckBoxes());
        tagsListEditor = new StringListInlineEditorComposite(Collections.<String> emptyList(), expandedUi);
        tagsListEditor.addValueChangeHandler(new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                validateAndUpdate();
            }
        });
    }

    /**
     * Creates a {@link ImageResizingTaskDTO} which contains an {@link MediaTagConstants} for every tag that does not fit to the boundaries of the image.
     * All tags that do not imply a resize will stay together on a single {@link ImageDTO} and are not stored in the {@link ImageResizingTaskDTO}.
     * There will be a {@link ImageDTO} for each of the other {@link MediaTagConstants}.
     * For a lookout to further progressing see {@link SailingService#resizeImage(ImageResizingTaskDTO)}
     */
    @Override
    protected ImageResizingTaskDTO getResult() {
        final List<String> tags = new ArrayList<String>();
        for (String tag : tagsListEditor.getValue()) {
            tags.add(tag);
        }
        final List<MediaTagConstants> resizingTask = new ArrayList<MediaTagConstants>();
        for (int i = 0; i < tags.size(); i++) {
            if (Arrays.asList(MediaTagConstants.values()).contains(MediaTagConstants.fromName(tags.get(i)))
                    && expandedUi.getCheckBoxes().get(i).getValue()) {
                resizingTask.add(MediaTagConstants.fromName(tags.get(i)));
            }
        }
        final ImageDTO image = new ImageDTO(imageURLAndUploadComposite.getURL(), creationDate);
        image.setTitle(titleTextBox.getValue());
        image.setSubtitle(subtitleTextBox.getValue());
        image.setCopyright(copyrightTextBox.getValue());
        if (widthInPxBox.getValue() != null && heightInPxBox.getValue() != null) {
            image.setSizeInPx(widthInPxBox.getValue(), heightInPxBox.getValue());
        }
        image.setTags(tags);
        return new ImageResizingTaskDTO(image, resizingTask);
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        Grid grid = new Grid(11, 2);

        grid.setWidget(0, 0, new Label(stringMessages.createdAt() + ":"));
        grid.setWidget(0, 1, createdAtLabel);
        grid.setWidget(1, 0, new HTML("&nbsp;"));
        grid.setWidget(1, 1, busyIndicator);
        grid.setWidget(2,  0, new Label(stringMessages.imageURL() + ":"));
        grid.setWidget(2, 1, imageURLAndUploadComposite);
        grid.setWidget(3, 0, new HTML("&nbsp;"));

        grid.setWidget(4,  0, new Label(stringMessages.title() + ":"));
        grid.setWidget(4, 1, titleTextBox);
        grid.setWidget(5,  0, new Label(stringMessages.subtitle() + ":"));
        grid.setWidget(5, 1, subtitleTextBox);
        grid.setWidget(6, 0, new Label(stringMessages.copyright() + ":"));
        grid.setWidget(6, 1, copyrightTextBox);
        grid.setWidget(7, 0, new Label(stringMessages.widthInPx() + ":"));
        grid.setWidget(7, 1, widthInPxBox);
        grid.setWidget(8, 0, new Label(stringMessages.heightInPx() + ":"));
        grid.setWidget(8, 1, heightInPxBox);

        grid.setWidget(9, 0, new HTML("&nbsp;"));
        grid.setWidget(10, 0, new Label(stringMessages.tags() + ":"));
        grid.setWidget(10, 1, tagsListEditor);

        panel.add(grid);

        return panel;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return imageURLAndUploadComposite.getInitialFocusWidget();
    }
    
    @Override
    public void onFileStorageServiceTestPassed() {
        imageURLAndUploadComposite.setUploadEnabled(true);
    }
}
