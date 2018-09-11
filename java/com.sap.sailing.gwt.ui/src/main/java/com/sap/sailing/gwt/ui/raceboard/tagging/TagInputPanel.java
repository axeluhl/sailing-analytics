package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;

/**
 * Panel containing input fields for tag/tag button creation and modification.
 */
public class TagInputPanel extends FlowPanel {

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    // default value for "Visible for public" checkbox
    private static final String DEFAULT_TAG = "";
    private static final String DEFAULT_COMMENT = "";
    private static final String DEFAULT_IMAGE_URL = "";
    private static final boolean DEFAULT_VISIBLE_FOR_PUBLIC = true;

    private final TextBox tagTextBox, imageURLTextBox;
    private final TextArea commentTextArea;
    private final CheckBox visibleForPublicCheckBox;

    /**
     * Creates view allowing users to input values for tags and {@link TagButton tag-buttons}.
     */
    protected TagInputPanel(StringMessages stringMessages) {
        setStyleName(style.tagInputPanel());

        tagTextBox = new TextBox();
        tagTextBox.setStyleName(style.tagInputPanelTag());
        tagTextBox.setTitle(stringMessages.tagLabelTag());
        tagTextBox.getElement().setAttribute("maxlength", Integer.toString(TagDTO.MAX_TAG_LENGTH));
        tagTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelTag());
        add(tagTextBox);

        imageURLTextBox = new TextBox();
        imageURLTextBox.setStyleName(style.tagInputPanelImageURL());
        imageURLTextBox.setTitle(stringMessages.tagLabelImageURL());
        imageURLTextBox.getElement().setPropertyString("placeholder", stringMessages.tagLabelImageURL());
        add(imageURLTextBox);

        commentTextArea = new TextArea();
        commentTextArea.setStyleName(style.tagInputPanelComment());
        commentTextArea.setVisibleLines(4);
        commentTextArea.getElement().setAttribute("maxlength", Integer.toString(TagDTO.MAX_COMMENT_LENGTH));
        commentTextArea.setTitle(stringMessages.tagLabelComment());
        commentTextArea.getElement().setPropertyString("placeholder", stringMessages.tagLabelComment());
        add(commentTextArea);

        SimplePanel checkboxWrapper = new SimplePanel();
        visibleForPublicCheckBox = new CheckBox(stringMessages.tagVisibleForPublicCheckBox());
        visibleForPublicCheckBox.setValue(DEFAULT_VISIBLE_FOR_PUBLIC);
        checkboxWrapper.setStyleName(style.tagInputPanelIsVisibleForPublic());
        checkboxWrapper.setWidget(visibleForPublicCheckBox);
        add(checkboxWrapper);
    }

    /**
     * Clears all input fields.
     */
    protected void clearAllValues() {
        tagTextBox.setText(DEFAULT_TAG);
        imageURLTextBox.setText(DEFAULT_COMMENT);
        commentTextArea.setText(DEFAULT_IMAGE_URL);
        setVisibleForPublic(DEFAULT_VISIBLE_FOR_PUBLIC);
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
        return getTag().equals(tag.getTag()) && getComment().equals(tag.getComment())
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
        return getTag().equals(tagButton.getTag()) && getComment().equals(tagButton.getComment())
                && getImageURL().equals(tagButton.getImageURL())
                && isVisibleForPublic() == tagButton.isVisibleForPublic();
    }

    /**
     * Returns whether input fields are equal to default values.
     * 
     * @return <code>true</code> if all input fields are equal to default values, otherwise <code>false</code>
     */
    protected boolean isInputEmpty() {
        return getTag().equals(DEFAULT_TAG) && getComment().equals(DEFAULT_COMMENT)
                && getImageURL().equals(DEFAULT_IMAGE_URL) && isVisibleForPublic() == DEFAULT_VISIBLE_FOR_PUBLIC;
    }

    protected String getTag() {
        return tagTextBox.getValue();
    }

    protected String getComment() {
        return commentTextArea.getValue();
    }

    protected String getImageURL() {
        return imageURLTextBox.getValue();
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
    }

    protected void setTag(String tag) {
        tagTextBox.setValue(tag);
    }

    protected void setComment(String comment) {
        commentTextArea.setValue(comment);
    }

    protected void setImageURL(String imageURL) {
        imageURLTextBox.setValue(imageURL);
    }

    protected void setVisibleForPublic(boolean visibleForPublic) {
        visibleForPublicCheckBox.setValue(visibleForPublic);
    }

    protected TextBox getTagTextBox() {
        return tagTextBox;
    }

    protected TextBox getImageURLTextBox() {
        return imageURLTextBox;
    }

    protected TextArea getCommentTextArea() {
        return commentTextArea;
    }

    protected CheckBox getVisibleForPublicCheckBox() {
        return visibleForPublicCheckBox;
    }
}
