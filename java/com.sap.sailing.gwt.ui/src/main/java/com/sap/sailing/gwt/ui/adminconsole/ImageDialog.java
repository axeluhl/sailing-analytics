package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.controls.listedit.StringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.media.ImageDTO;

public abstract class ImageDialog extends DataEntryDialog<ImageDTO> {
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
    
    protected static class ImageParameterValidator implements Validator<ImageDTO> {
        private StringMessages stringMessages;

        public ImageParameterValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(ImageDTO imageToValidate) {
            String errorMessage = null;
            
            if(imageToValidate.getSourceRef() == null || imageToValidate.getSourceRef().isEmpty()) {
                errorMessage = stringMessages.pleaseEnterNonEmptyUrl();
            }
            return errorMessage;
        }
    }

    public ImageDialog(ImageParameterValidator validator, StringMessages stringMessages, DialogCallback<ImageDTO> callback) {
        super(stringMessages.image(), null, stringMessages.ok(), stringMessages.cancel(), validator,
                callback);
        this.stringMessages = stringMessages;
        this.creationDate = new Date();
        getDialogBox().getWidget().setWidth("730px");

        imageURLAndUploadComposite = new URLFieldWithFileUpload(stringMessages);
        imageURLAndUploadComposite.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validate();
            }
        });

        final List<String> tagSuggestions = Arrays.asList(new String[] { "Stage", "Teaser", "Sponsor", "Logo" });
        tagsListEditor = new StringListInlineEditorComposite(Collections.<String> emptyList(),
                new StringListInlineEditorComposite.ExpandedUi(stringMessages, IconResources.INSTANCE.removeIcon(), /* suggestValues */
                        tagSuggestions, "Enter tags for the image", 50));
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

        Grid formGrid = new Grid(8, 2);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label("Created at:"));
        formGrid.setWidget(0, 1, createdAtLabel);
        formGrid.setWidget(1,  0, new Label("Image URL:"));
        formGrid.setWidget(1, 1, imageURLAndUploadComposite);
        formGrid.setWidget(2,  0, new Label(stringMessages.title() + ":"));
        formGrid.setWidget(2, 1, titleTextBox);
        formGrid.setWidget(3,  0, new Label("Subtitle:"));
        formGrid.setWidget(3, 1, subtitleTextBox);
        formGrid.setWidget(4, 0, new Label("Copyright:"));
        formGrid.setWidget(4, 1, copyrightTextBox);
        formGrid.setWidget(5, 0, new Label("Width in px:"));
        formGrid.setWidget(5, 1, widthInPxBox);
        formGrid.setWidget(6, 0, new Label("Height in px:"));
        formGrid.setWidget(6, 1, heightInPxBox);
        formGrid.setWidget(7, 0, new Label("Tags:"));
        formGrid.setWidget(7, 1, tagsListEditor);

        return panel;
    }

    @Override
    public void show() {
        super.show();
        imageURLAndUploadComposite.setFocus(true);
    }
}
