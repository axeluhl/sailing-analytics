package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
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
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.media.ImageDTO;

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
    protected SimplePanel imageHolder;

    protected static class ImageParameterValidator implements Validator<ImageDTO> {
        private StringMessages stringMessages;

        public ImageParameterValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(ImageDTO imageToValidate) {
            String errorMessage = null;
            Integer imageWidth = imageToValidate.getWidthInPx();
            Integer imageHeight = imageToValidate.getHeightInPx();
            
            if(imageToValidate.getSourceRef() == null || imageToValidate.getSourceRef().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrl();
            } else if(imageWidth == null || imageHeight == null) {
                errorMessage = "The width and height of the image could not retrieved yet.";
            } else if(imageToValidate.hasTag(MediaTagConstants.LOGO) && !isValidSize(imageWidth, imageHeight, MediaConstants.MIN_LOGO_IMAGE_WIDTH,
                    MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT)) {
                errorMessage = getSizeErrorMessage("Logo", MediaConstants.MIN_LOGO_IMAGE_WIDTH,
                        MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT, MediaConstants.MAX_LOGO_IMAGE_HEIGHT);
            } else if(imageToValidate.hasTag(MediaTagConstants.TEASER) && !isValidSize(imageWidth, imageHeight, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH,
                    MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT)) {
                errorMessage = getSizeErrorMessage("Event-Teaser", MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH,
                        MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT, MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT);
            } else if(imageToValidate.hasTag(MediaTagConstants.STAGE) && !isValidSize(imageWidth, imageHeight, MediaConstants.MIN_STAGE_IMAGE_WIDTH,
                    MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT)) {
                errorMessage = getSizeErrorMessage("Stage", MediaConstants.MIN_STAGE_IMAGE_WIDTH,
                        MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT, MediaConstants.MAX_STAGE_IMAGE_HEIGHT);
            }
            return errorMessage;
        }
        
        private boolean isValidSize(int width, int height, int minWidth, int maxWidth, int minHeight, int maxHeight) {
            return width >= minWidth && width <= maxWidth && height >= minHeight && height <= maxHeight;
        }
        
        private String getSizeErrorMessage(String imageType, int minWidth, int maxWidth, int minHeight, int maxHeight) {
            String errorMessage = "The size of the " + imageType + " image does not fit. ";
            errorMessage += "The width should be between " + minWidth + " and " + maxWidth + " px ";
            errorMessage += " and the height between " + minHeight + " and " + maxHeight + " px.";
            return errorMessage;
        }
    }

    public ImageDialog(Date creationDate, ImageParameterValidator validator, SailingServiceAsync sailingService, StringMessages stringMessages, DialogCallback<ImageDTO> callback) {
        super(stringMessages.image(), null, stringMessages.ok(), stringMessages.cancel(), validator,
                callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.creationDate = creationDate;
        getDialogBox().getWidget().setWidth("730px");

        imageHolder = new SimplePanel();

        imageURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages);
        imageURLAndUploadComposite.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String imageUrlAsString = event.getValue();
                ImageDialog.this.sailingService.resolveImageDimensions(imageUrlAsString, new AsyncCallback<Util.Pair<Integer,Integer>>() {
                    @Override
                    public void onSuccess(Pair<Integer, Integer> imageSize) {
                        if(imageSize != null) {
                            widthInPxBox.setValue(imageSize.getA());
                            heightInPxBox.setValue(imageSize.getB());
                        }
                        validate();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                    }
                });
                
//                image = loadImageFromURL(event.getValue());
//                imageHolder.setWidget(image);
//                image.getElement().getStyle().setBorderWidth(1, Unit.PX);
//                image.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
//                image.getElement().getStyle().setBorderColor("#cccccc");
                validate();
            }
        });
        
        tagsListEditor = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new StringListInlineEditorComposite.ExpandedUi(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        MediaConstants.imageTagSuggestions, "Enter tags for the image", 30));
    }
    
    @Override
    protected ImageDTO getResult() {
        ImageDTO result = new ImageDTO(imageURLAndUploadComposite.getURL(), creationDate);
        result.setTitle(titleTextBox.getValue());
        result.setSubtitle(subtitleTextBox.getValue());
        result.setCopyright(copyrightTextBox.getValue());
        if(widthInPxBox.getValue() != null && heightInPxBox.getValue() != null) {
            result.setSizeInPx(widthInPxBox.getValue(), heightInPxBox.getValue());
        }
        List<String> tags = new ArrayList<String>();
        for (String tag: tagsListEditor.getValue()) {
            tags.add(tag);
        }
        result.setTags(tags);
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }

        HorizontalPanel hPanel = new HorizontalPanel();
     
        Grid grid1 = new Grid(2, 2);
        Grid grid2 = new Grid(6, 2);
        Grid grid3 = new Grid(1, 2);

        grid1.setWidget(0, 0, new Label("Created at:"));
        grid1.setWidget(0, 1, createdAtLabel);
        grid1.setWidget(1,  0, new Label("Image URL:"));
        grid1.setWidget(1, 1, imageURLAndUploadComposite);
        
        grid2.setWidget(0,  0, new Label(stringMessages.title() + ":"));
        grid2.setWidget(0, 1, titleTextBox);
        grid2.setWidget(1,  0, new Label("Subtitle:"));
        grid2.setWidget(1, 1, subtitleTextBox);
        grid2.setWidget(2, 0, new Label("Copyright:"));
        grid2.setWidget(2, 1, copyrightTextBox);
        grid2.setWidget(3, 0, new Label("Width in px:"));
        grid2.setWidget(3, 1, widthInPxBox);
        grid2.setWidget(4, 0, new Label("Height in px:"));
        grid2.setWidget(4, 1, heightInPxBox);

        grid3.setWidget(0, 0, new Label("Tags:"));
        grid3.setWidget(0, 1, tagsListEditor);

        panel.add(grid1);
        hPanel.add(grid2);
        hPanel.add(imageHolder);
        hPanel.setCellWidth(grid1, "50%");
        hPanel.setCellWidth(imageHolder, "50%");
        panel.add(hPanel);
        panel.add(grid3);

        return panel;
    }

    @Override
    public void show() {
        super.show();
        imageURLAndUploadComposite.setFocus(true);
    }
    
    protected Image loadImageFromURL(String url) {
        final int maxImageHolderWidth = 300;
        final int maxImageHolderHeight = 300;
        final Image image = new Image(url);
        image.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                int width = image.getOffsetWidth();
                int height = image.getOffsetHeight();  
                if(width > 0 && height > 0) {
                    widthInPxBox.setValue(width);
                    heightInPxBox.setValue(height);
                }
                Pair<Integer, Integer> fitToBox = fitSizeToBox(maxImageHolderWidth, maxImageHolderHeight, width, height, true);
                image.setWidth(fitToBox.getA() + "px");
                image.setHeight(fitToBox.getB() + "px");
                validate();
            }
        });
        return image;
    }
    
    private Util.Pair<Integer, Integer> fitSizeToBox(int boxWidth, int boxHeight, int imageWidth, int imageHeight, boolean neverScaleUp) {
        double scale = Math.min((double) boxWidth / (double) imageWidth, (double) boxHeight / (double) imageHeight);

        int h = (int) (!neverScaleUp || scale < 1.0 ? scale * imageHeight : imageHeight);
        int w = (int) (!neverScaleUp || scale < 1.0 ? scale * imageWidth : imageWidth);
        
        return new Util.Pair<Integer, Integer>(w,h);
    }
}
