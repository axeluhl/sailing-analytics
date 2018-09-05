package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;

/**
 * Panel containing input fields for tag/tag button creation and modification.
 */
public class TagInputPanel extends FlowPanel {

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    // default value for "Visible for public" checkbox
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
        tagTextBox.setText("");
        imageURLTextBox.setText("");
        commentTextArea.setText("");
        setVisibleForPublic(DEFAULT_VISIBLE_FOR_PUBLIC);
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
