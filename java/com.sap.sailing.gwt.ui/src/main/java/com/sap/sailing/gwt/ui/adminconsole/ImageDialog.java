package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorWithCheckboxesComposite;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ToResizeImageDTO;

public abstract class ImageDialog extends DataEntryDialog<ImageDTO> {
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
    protected StringListInlineEditorComposite tagsListEditor;
    protected Image image;
    protected List<CheckBox> doResize;
    protected Label doResizeLabel;
    private final BusyIndicator busyIndicator;

    protected static class ImageParameterValidator implements Validator<ImageDTO> {
        private StringMessages stringMessages;
        private List<CheckBox> doResize;
        private Label doResizeLabel;
        
        public ImageParameterValidator(StringMessages stringMessages, ArrayList<CheckBox> doResize, Label doResizeLabel) {
            this.stringMessages = stringMessages;
            this.doResize = doResize;
            this.doResizeLabel = doResizeLabel;
        }
        
        @Override
        public String getErrorMessage(ImageDTO imageToValidate) {
            String errorMessage = null;
            Integer imageWidth = imageToValidate.getWidthInPx();
            Integer imageHeight = imageToValidate.getHeightInPx();
            
            for(CheckBox checkBox : doResize) {//set all invisible, so they are updated for all errors that occure before a resizing error in following construct
                checkBox.setVisible(false);
            }
            
            if (imageToValidate.getSourceRef() == null || imageToValidate.getSourceRef().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrlOrUploadImage();
            } else if (imageWidth == null || imageHeight == null) {
                errorMessage = stringMessages.couldNotRetrieveImageSizeYet();
            } else {
                //check if image is too small for resizing
                errorMessage = "";
                if (imageToValidate.hasTag(MediaTagConstants.LOGO) && (imageWidth < MediaConstants.MIN_LOGO_IMAGE_WIDTH || imageHeight < MediaConstants.MIN_LOGO_IMAGE_HEIGHT)) {
                    errorMessage += getImageToSmallErrorMessage(MediaTagConstants.LOGO, MediaConstants.MIN_LOGO_IMAGE_WIDTH,
                            MediaConstants.MIN_LOGO_IMAGE_HEIGHT, stringMessages) + "\n";
                }
                if (imageToValidate.hasTag(MediaTagConstants.TEASER) && (imageWidth < MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH || imageHeight < MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT)) {
                    errorMessage += getImageToSmallErrorMessage(MediaTagConstants.TEASER, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH,
                            MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, stringMessages) + "\n";
                }
                if (imageToValidate.hasTag(MediaTagConstants.STAGE) && (imageWidth < MediaConstants.MIN_STAGE_IMAGE_WIDTH || imageHeight < MediaConstants.MIN_STAGE_IMAGE_HEIGHT)) {
                    errorMessage += getImageToSmallErrorMessage(MediaTagConstants.STAGE, MediaConstants.MIN_STAGE_IMAGE_WIDTH,
                            MediaConstants.MIN_STAGE_IMAGE_HEIGHT, stringMessages) + "\n";
                }
                if(errorMessage.equals("")) {//Check if image ratio fits for resizing
                    errorMessage = imageRatioFits(imageToValidate);
                }
                if(errorMessage.equals("")) {//check for ckeckboxes and resizing
                    if (imageToValidate.hasTag(MediaTagConstants.LOGO) && !isValidSize(imageWidth, imageHeight,
                            MediaConstants.MIN_LOGO_IMAGE_WIDTH, MediaConstants.MAX_LOGO_IMAGE_WIDTH,
                            MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT)) {
                        getCheckBoxForTag(MediaTagConstants.LOGO, imageToValidate).setVisible(true);
                        if(!getCheckBoxForTag(MediaTagConstants.LOGO, imageToValidate).getValue()) {
                            errorMessage += getSizeErrorMessage(MediaTagConstants.LOGO, MediaConstants.MIN_LOGO_IMAGE_WIDTH,
                                    MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT,
                                    MediaConstants.MAX_LOGO_IMAGE_HEIGHT, stringMessages) + "\n";
                        }
                    }
                    if (imageToValidate.hasTag(MediaTagConstants.TEASER) && !isValidSize(imageWidth, imageHeight,
                            MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH,
                            MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT)) {
                        getCheckBoxForTag(MediaTagConstants.TEASER, imageToValidate).setVisible(true);
                        if(!getCheckBoxForTag(MediaTagConstants.TEASER, imageToValidate).getValue()) {
                            errorMessage += getSizeErrorMessage(MediaTagConstants.TEASER, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH,
                                    MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT,
                                    MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT, stringMessages) + "\n";
                        }
                    }
                    if (imageToValidate.hasTag(MediaTagConstants.STAGE) && !isValidSize(imageWidth, imageHeight,
                            MediaConstants.MIN_STAGE_IMAGE_WIDTH, MediaConstants.MAX_STAGE_IMAGE_WIDTH,
                            MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT)) {
                        getCheckBoxForTag(MediaTagConstants.STAGE, imageToValidate).setVisible(true);
                        if(!getCheckBoxForTag(MediaTagConstants.STAGE, imageToValidate).getValue()) {
                            errorMessage += getSizeErrorMessage(MediaTagConstants.STAGE, MediaConstants.MIN_STAGE_IMAGE_WIDTH,
                                    MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT,
                                    MediaConstants.MAX_STAGE_IMAGE_HEIGHT, stringMessages) + "\n";
                        }
                    }
                }
            }
            for(CheckBox checkBox : doResize) {//all checkboxes that are set to invisible will now be set it's value to false
                if(!checkBox.isVisible()) {
                    checkBox.setValue(false);
                }
            }
            if(checkBoxIsVisible()) {
                doResizeLabel.setVisible(true);
            }else {
                doResizeLabel.setVisible(false);
            }
            if(errorMessage.equals(""))
               return null;
            return errorMessage;
        }
        
        private boolean checkBoxIsVisible() {
            for(CheckBox checkBox : doResize) {
                if(checkBox.isVisible())
                    return true;
            }
            return false;
        }

        private CheckBox getCheckBoxForTag(String tag, ImageDTO imageToValidate) {
            List<String> tags = imageToValidate.getTags();
            for(int i = 0; i < tags.size(); i++) {
                if(tags.get(i).equals(tag)) {
                    return doResize.get(i);
                }
            }
            return new CheckBox();//new checkbox instead of null, so there is no need for a null check. this will be deleted from garbage collector anyway
        }
        
        private String imageRatioFits(ImageDTO imageToValidate) {
            String errorMessage = "";
            double ratio = ((double)imageToValidate.getWidthInPx())/imageToValidate.getHeightInPx();
            double minRatio = ((double)MediaConstants.MAX_LOGO_IMAGE_WIDTH)/MediaConstants.MIN_LOGO_IMAGE_HEIGHT;
            double maxRatio = ((double)MediaConstants.MIN_LOGO_IMAGE_WIDTH)/MediaConstants.MAX_LOGO_IMAGE_HEIGHT;
            if(imageToValidate.hasTag(MediaTagConstants.LOGO) && (minRatio < ratio || maxRatio > ratio)) {
                errorMessage += stringMessages.imageResizeError(MediaTagConstants.LOGO, minRatio, maxRatio, ratio);
            }
            minRatio = ((double)MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH)/MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT;
            maxRatio = ((double)MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH)/MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT;
            if(imageToValidate.hasTag(MediaTagConstants.TEASER) && (minRatio < ratio || maxRatio > ratio)) {
                errorMessage += stringMessages.imageResizeError(MediaTagConstants.TEASER, minRatio, maxRatio, ratio);
            }
            minRatio = ((double)MediaConstants.MAX_STAGE_IMAGE_WIDTH)/MediaConstants.MIN_STAGE_IMAGE_HEIGHT;
            maxRatio = ((double)MediaConstants.MIN_STAGE_IMAGE_WIDTH)/MediaConstants.MAX_STAGE_IMAGE_HEIGHT;
            if(imageToValidate.hasTag(MediaTagConstants.STAGE) && (minRatio < ratio || maxRatio > ratio)) {
                errorMessage += stringMessages.imageResizeError(MediaTagConstants.STAGE, minRatio, maxRatio, ratio);
            }
            return errorMessage;
        }

        private boolean isValidSize(int width, int height, int minWidth, int maxWidth, int minHeight, int maxHeight) {
            return width >= minWidth && width <= maxWidth && height >= minHeight && height <= maxHeight;
        }
        
        private String getSizeErrorMessage(String imageType, int minWidth, int maxWidth, int minHeight, int maxHeight, StringMessages stringMessages) {
            String errorMessage = stringMessages.imageSizeError(imageType, minWidth, maxWidth, minHeight, maxHeight);
            return errorMessage;
        }
        
        private String getImageToSmallErrorMessage(String imageType, int minWidth, int minHeight, StringMessages stringMessages) {
            String errorMessage = stringMessages.imageToSmallError(imageType,minWidth,minHeight);
            return errorMessage;
        }
    }
    public ImageDialog(Date creationDate, SailingServiceAsync sailingService, StringMessages stringMessages, DialogCallback<ImageDTO> callback) {
        this(creationDate, sailingService, stringMessages, new ArrayList<>(), new Label(stringMessages.allowResizing()), callback);
    }
    
    private ImageDialog(Date creationDate, SailingServiceAsync sailingService, StringMessages stringMessages, ArrayList<CheckBox> doResize, Label doResizeLabel, DialogCallback<ImageDTO> callback) {
        super(stringMessages.image(), null, stringMessages.ok(), stringMessages.cancel(), new ImageParameterValidator(stringMessages, doResize, doResizeLabel), callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.creationDate = creationDate;
        this.doResize = doResize;
        this.doResizeLabel = doResizeLabel;
        getDialogBox().getWidget().setWidth("730px");
        busyIndicator = new SimpleBusyIndicator();
        imageURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages);
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
        
        doResizeLabel.setVisible(false);
        tagsListEditor = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new GenericStringListInlineEditorWithCheckboxesComposite.ExpandedUi<String>(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        MediaConstants.imageTagSuggestions, stringMessages.enterTagsForTheImage(), 30, doResize,doResizeLabel,new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                validateAndUpdate();
                                CheckBox checkBox = (CheckBox)event.getSource();
                                if(checkBox.getValue()) {
                                    checkBox.getElement().getStyle().clearBackgroundColor();
                                }else {
                                    checkBox.getElement().getStyle().setBackgroundColor("red");
                                }  
                                
                            }
                        }));
        tagsListEditor.addValueChangeHandler(new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                validateAndUpdate();
            }
        });
    }
    
    @Override
    protected ImageDTO getResult() {
        List<String> tags = new ArrayList<String>();
        for (String tag: tagsListEditor.getValue()) {
            tags.add(tag);
        }
        HashMap<String,Boolean> map = new HashMap<String, Boolean>();
        for(int i= 0; i < doResize.size(); i++) {
            if(tags.get(i).equals(MediaTagConstants.LOGO) || tags.get(i).equals(MediaTagConstants.TEASER) || tags.get(i).equals(MediaTagConstants.STAGE) || tags.get(i).equals(MediaTagConstants.GALLERY)) {
                map.put(tags.get(i), doResize.get(i).getValue());
            }
        }
        ImageDTO result = new ToResizeImageDTO(imageURLAndUploadComposite.getURL(), creationDate, map);
        result.setTitle(titleTextBox.getValue());
        result.setSubtitle(subtitleTextBox.getValue());
        result.setCopyright(copyrightTextBox.getValue());
        if (widthInPxBox.getValue() != null && heightInPxBox.getValue() != null) {
            result.setSizeInPx(widthInPxBox.getValue(), heightInPxBox.getValue());
        }
        result.setTags(tags);
        return (ImageDTO)result;
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
}
