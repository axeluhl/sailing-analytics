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

    public TagInputPanel(StringMessages stringMessages) {
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
        imageURLTextBox.getElement().setAttribute("maxlength", Integer.toString(TagDTO.MAX_IMAGE_URL_LENGTH));
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

    public TextBox getTagTextBox() {
        return tagTextBox;
    }

    public TextBox getImageURLTextBox() {
        return imageURLTextBox;
    }

    public TextArea getCommentTextArea() {
        return commentTextArea;
    }

    public CheckBox getVisibleForPublicCheckBox() {
        return visibleForPublicCheckBox;
    }

    public String getTag() {
        return tagTextBox.getValue();
    }

    public String getComment() {
        return commentTextArea.getValue();
    }

    public String getImageURL() {
        return imageURLTextBox.getValue();
    }

    public boolean isVisibleForPublic() {
        return visibleForPublicCheckBox.getValue();
    }

    public void setTag(String tag) {
        tagTextBox.setValue(tag);
    }

    public void setComment(String comment) {
        commentTextArea.setValue(comment);
    }

    public void setImageURL(String imageURL) {
        imageURLTextBox.setValue(imageURL);
    }

    public void setVisibleForPublic(boolean visibleForPublic) {
        visibleForPublicCheckBox.setValue(visibleForPublic);
    }

    public void clearAllValues() {
        tagTextBox.setText("");
        imageURLTextBox.setText("");
        commentTextArea.setText("");
        setVisibleForPublic(DEFAULT_VISIBLE_FOR_PUBLIC);
    }
}
