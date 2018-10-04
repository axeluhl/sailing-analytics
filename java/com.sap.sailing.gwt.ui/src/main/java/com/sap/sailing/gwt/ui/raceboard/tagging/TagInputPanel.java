package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.adminconsole.URLFieldWithFileUpload;

/**
 * Panel containing input fields for tag/tag button creation and modification.
 */
// TODO: use DataEntryDialog
public class TagInputPanel extends FlowPanel {

    private final TagPanelStyle style = TaggingPanelResources.INSTANCE.style();

    private static final String DEFAULT_TAG = "";
    private static final String DEFAULT_COMMENT = "";
    private static final String DEFAULT_IMAGE_URL = "";
    private static final boolean DEFAULT_VISIBLE_FOR_PUBLIC = false;

    private final TaggingPanel taggingPanel;
    private final Grid grid;
    private final TextBox tagTextBox;
    private final URLFieldWithFileUpload imageUrlUploaderPanel;
    private final TextArea commentTextArea;
    private final FlowPanel checkboxWrapper;
    private final CheckBox visibleForPublicCheckBox;
    private final Label noPermissionForPublicTagsLabel;

    private int imageWidth = -1;
    private int imageHeight = -1;

    /**
     * Creates view allowing users to input values for tags and {@link TagButton tag-buttons}.
     */
    protected TagInputPanel(TaggingPanel taggingPanel, SailingServiceAsync sailingService,
            StringMessages stringMessages) {
        setStyleName(style.tagInputPanel());
        this.taggingPanel = taggingPanel;
        grid = new Grid(4, 2);
        // tag
        Label tagLabel = new Label(stringMessages.tagLabelTag() + ":");
        tagTextBox = new TextBox();
        tagTextBox.setStyleName(style.tagInputPanelTag());
        tagTextBox.setTitle(stringMessages.tagLabelTag());
        tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
        grid.setWidget(0, 0, tagLabel);
        grid.setWidget(0, 1, tagTextBox);
        // image upload
        Label imageLabel = new Label(stringMessages.tagLabelImage() + ":");
        imageUrlUploaderPanel = new URLFieldWithFileUpload(stringMessages);
        imageUrlUploaderPanel.addValueChangeHandler(event -> {
            if (imageUrlUploaderPanel.getURL() == null || imageUrlUploaderPanel.getURL().isEmpty()) {
                imageWidth = -1;
                imageHeight = -1;
            } else {
                sailingService.resolveImageDimensions(imageUrlUploaderPanel.getURL(),
                        new AsyncCallback<Util.Pair<Integer, Integer>>() {
                            @Override
                            public void onSuccess(Pair<Integer, Integer> imageSize) {
                                if (imageSize != null) {
                                    imageWidth = imageSize.getA();
                                    imageHeight = imageSize.getB();
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                imageUrlUploaderPanel.setURL("");
                            }
                        });
            }
        });
        grid.setWidget(1, 0, imageLabel);
        grid.setWidget(1, 1, imageUrlUploaderPanel);
        // comment
        Label commentLabel = new Label(stringMessages.tagLabelComment() + ":");
        commentTextArea = new TextArea();
        commentTextArea.setStyleName(style.tagInputPanelComment());
        commentTextArea.setVisibleLines(4);
        commentTextArea.setTitle(stringMessages.tagLabelComment());
        commentTextArea.getElement().setPropertyString("placeholder", stringMessages.tagLabelComment());
        grid.setWidget(2, 0, commentLabel);
        grid.setWidget(2, 1, commentTextArea);
        // visibility
        Label visibilityLabel = new Label(stringMessages.tagLabelVisibility() + ":");
        visibleForPublicCheckBox = new CheckBox(stringMessages.tagVisibleForPublicCheckBox());
        visibleForPublicCheckBox.setValue(DEFAULT_VISIBLE_FOR_PUBLIC);
        noPermissionForPublicTagsLabel = new Label(stringMessages.tagPublicModificationPermissionMissing());
        checkboxWrapper = new FlowPanel();
        checkboxWrapper.add(visibleForPublicCheckBox);
        checkboxWrapper.add(noPermissionForPublicTagsLabel);
        grid.setWidget(3, 0, visibilityLabel);
        grid.setWidget(3, 1, checkboxWrapper);

        add(grid);
        clearAllValues();
    }

    public String getImageURL() {
        return imageUrlUploaderPanel.getURL();
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Clears all input fields.
     */
    protected void clearAllValues() {
        setTag(DEFAULT_TAG);
        setComment(DEFAULT_COMMENT);
        setImageURL(DEFAULT_IMAGE_URL);
        setVisibleForPublic(DEFAULT_VISIBLE_FOR_PUBLIC);
        setCurrentStatus();
    }

    /**
     * Returns whether values of given <code>tag</code> are different from input fields.
     * 
     * @param tag
     *            {@link TagDTO} to compare to input fields
     * @return <code>true</code> if title, comment, imageURL and visibility are equal to input fields, otherwise
     *         <code>false</code>
     */
    protected boolean compareFieldsToTag(TagDTO tag) {
        return tag != null && getTag().equals(tag.getTag()) && getComment().equals(tag.getComment())
                && getImageURL().equals(tag.getImageURL()) && isVisibleForPublic() == tag.isVisibleForPublic();
    }

    /**
     * Returns whether values of given <code>tagButton</code> are different from input fields.
     * 
     * @param tagButton
     *            {@link TagButton} to compare to input fields
     * @return <code>true</code> if title, comment, imageURL and visibility are equal to input fields, otherwise
     *         <code>false</code>
     */
    protected boolean compareFieldsToTagButton(TagButton tagButton) {
        return tagButton != null && getTag().equals(tagButton.getTag()) && getComment().equals(tagButton.getComment())
                && getImageURL().equals(tagButton.getImageURL())
                && isVisibleForPublic() == tagButton.isVisibleForPublic();
    }

    /**
     * Returns whether input fields are equal to default values.
     * 
     * @return <code>true</code> if all input fields are equal to default values, otherwise <code>false</code>
     */
    protected boolean isInputEmpty() {
        return (getTag() == null || getTag().equals(DEFAULT_TAG))
                && (getComment() == null || getComment().equals(DEFAULT_COMMENT))
                && (getImageURL() == null || getImageURL().equals(DEFAULT_IMAGE_URL))
                && isVisibleForPublic() == DEFAULT_VISIBLE_FOR_PUBLIC;
    }

    protected String getTag() {
        return tagTextBox.getValue();
    }

    protected String getComment() {
        return commentTextArea.getValue();
    }

    protected boolean isVisibleForPublic() {
        return visibleForPublicCheckBox.getValue();
    }

    /**
     * Sets all input fields to values of given {@link TagDTO tag}.
     * 
     * @param tag
     *            tag
     */
    protected void setTag(TagDTO tag) {
        setTag(tag.getTag());
        setComment(tag.getComment());
        setImageURL(tag.getImageURL());
        setVisibleForPublic(tag.isVisibleForPublic());
        setCurrentStatus();
    }

    protected void setTag(String tag) {
        tagTextBox.setValue(tag);
    }

    protected void setComment(String comment) {
        commentTextArea.setValue(comment);
    }

    protected void setImageURL(String imageURL) {
        imageUrlUploaderPanel.setValue(imageURL);
    }

    protected void setVisibleForPublic(boolean visibleForPublic) {
        visibleForPublicCheckBox.setValue(visibleForPublic);
    }

    protected TextBox getTagTextBox() {
        return tagTextBox;
    }

    protected URLFieldWithFileUpload getImageURLTextBox() {
        return imageUrlUploaderPanel;
    }

    protected TextArea getCommentTextArea() {
        return commentTextArea;
    }

    protected CheckBox getVisibleForPublicCheckBox() {
        return visibleForPublicCheckBox;
    }

    protected void setCurrentStatus() {
        if (taggingPanel.hasPermissionToModifyPublicTags()) {
            visibleForPublicCheckBox.setVisible(true);
            noPermissionForPublicTagsLabel.setVisible(false);
        } else {
            visibleForPublicCheckBox.setVisible(false);
            noPermissionForPublicTagsLabel.setVisible(true);
        }
    }
}