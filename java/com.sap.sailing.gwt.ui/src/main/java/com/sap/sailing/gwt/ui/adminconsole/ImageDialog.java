package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
    private final BusyIndicator busyIndicator;

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
            
            if (imageToValidate.getSourceRef() == null || imageToValidate.getSourceRef().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrl();
            } else if (imageWidth == null || imageHeight == null) {
                errorMessage = "The width and height of the image could not retrieved yet.";
            } else if (imageToValidate.hasTag(MediaTagConstants.LOGO)
                    && !isValidSize(imageWidth, imageHeight, MediaConstants.MIN_LOGO_IMAGE_WIDTH,
                            MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT,
                            MediaConstants.MAX_LOGO_IMAGE_HEIGHT)) {
                errorMessage = getSizeErrorMessage("Logo", MediaConstants.MIN_LOGO_IMAGE_WIDTH,
                        MediaConstants.MAX_LOGO_IMAGE_WIDTH, MediaConstants.MIN_LOGO_IMAGE_HEIGHT,
                        MediaConstants.MAX_LOGO_IMAGE_HEIGHT);
            } else if (imageToValidate.hasTag(MediaTagConstants.TEASER)
                    && !isValidSize(imageWidth, imageHeight, MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH,
                            MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT,
                            MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT)) {
                errorMessage = getSizeErrorMessage("Event-Teaser", MediaConstants.MIN_EVENTTEASER_IMAGE_WIDTH,
                        MediaConstants.MAX_EVENTTEASER_IMAGE_WIDTH, MediaConstants.MIN_EVENTTEASER_IMAGE_HEIGHT,
                        MediaConstants.MAX_EVENTTEASER_IMAGE_HEIGHT);
            } else if (imageToValidate.hasTag(MediaTagConstants.STAGE)
                    && !isValidSize(imageWidth, imageHeight, MediaConstants.MIN_STAGE_IMAGE_WIDTH,
                            MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT,
                            MediaConstants.MAX_STAGE_IMAGE_HEIGHT)) {
                errorMessage = getSizeErrorMessage("Stage", MediaConstants.MIN_STAGE_IMAGE_WIDTH,
                        MediaConstants.MAX_STAGE_IMAGE_WIDTH, MediaConstants.MIN_STAGE_IMAGE_HEIGHT,
                        MediaConstants.MAX_STAGE_IMAGE_HEIGHT);
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
        super(stringMessages.image(), null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.creationDate = creationDate;
        getDialogBox().getWidget().setWidth("730px");
        busyIndicator = new SimpleBusyIndicator();
        imageURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages);
        imageURLAndUploadComposite.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                busyIndicator.setBusy(true);
                String imageUrlAsString = event.getValue();
                ImageDialog.this.sailingService.resolveImageDimensions(imageUrlAsString, new AsyncCallback<Util.Pair<Integer,Integer>>() {
                    @Override
                    public void onSuccess(Pair<Integer, Integer> imageSize) {
                        busyIndicator.setBusy(false);
                        if (imageSize != null) {
                            widthInPxBox.setValue(imageSize.getA());
                            heightInPxBox.setValue(imageSize.getB());
                        }
                        validate();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        busyIndicator.setBusy(false);
                    }
                });
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
        if (widthInPxBox.getValue() != null && heightInPxBox.getValue() != null) {
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

        Grid grid = new Grid(11, 2);

        grid.setWidget(0, 0, new Label("Created at:"));
        grid.setWidget(0, 1, createdAtLabel);
        grid.setWidget(1, 0, new HTML("&nbsp;"));
        grid.setWidget(1, 1, busyIndicator);
        grid.setWidget(2,  0, new Label("Image URL:"));
        grid.setWidget(2, 1, imageURLAndUploadComposite);
        grid.setWidget(3, 0, new HTML("&nbsp;"));

        grid.setWidget(4,  0, new Label(stringMessages.title() + ":"));
        grid.setWidget(4, 1, titleTextBox);
        grid.setWidget(5,  0, new Label("Subtitle:"));
        grid.setWidget(5, 1, subtitleTextBox);
        grid.setWidget(6, 0, new Label("Copyright:"));
        grid.setWidget(6, 1, copyrightTextBox);
        grid.setWidget(7, 0, new Label("Width in px:"));
        grid.setWidget(7, 1, widthInPxBox);
        grid.setWidget(8, 0, new Label("Height in px:"));
        grid.setWidget(8, 1, heightInPxBox);

        grid.setWidget(9, 0, new HTML("&nbsp;"));
        grid.setWidget(10, 0, new Label("Tags:"));
        grid.setWidget(10, 1, tagsListEditor);

        panel.add(grid);

        return panel;
    }

    @Override
    public void show() {
        super.show();
        imageURLAndUploadComposite.setFocus(true);
    }
}
