package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Panel containg TagCreationPanel and TagButtonPanel which is used as footer in TaggingPanel.
 */
public class TagFooterPanel extends FlowPanel {

    private final TaggingPanel taggingPanel;

    private final TagCreationPanel tagCreationPanel;
    private final TagButtonPanel tagButtonPanel;

    public TagFooterPanel(TaggingPanel taggingPanel) {
        this.taggingPanel = taggingPanel;

        tagCreationPanel = new TagCreationPanel(this, taggingPanel);
        tagButtonPanel = new TagButtonPanel(this, taggingPanel);

        // Tag buttons are only shown if amount of tag buttons is greater then 0!
        setTagButtonsVisibility(true);

        // input fields are hidden by default
        setInputFieldsVisibility(false);

        tagButtonPanel.loadAllTagButtons();
    }

    /**
     * Sets visibility of input fields for new tag and "Edit Tag-Buttons"-button. Input fields will not be hidden but
     * removed completly from DOM in case parameter visible is set to false!
     * 
     * @param visible
     *            should be true when user is logged in and tagging panel is in EDIT-mode, otherwise false
     */
    protected void setInputFieldsVisibility(boolean visible) {
        if (visible) {
            add(tagCreationPanel);
            taggingPanel.refreshFooterPanel();
        } else {
            remove(tagCreationPanel);
            taggingPanel.refreshFooterPanel();
        }
    }

    /**
     * Sets visibility of tag buttons. Tag buttons panel will only be shown if amount of tag buttons is greater then 0!
     * 
     * @param visible
     *            should be true when user is logged in, otherwise false
     */
    protected void setTagButtonsVisibility(boolean visible) {
        if (taggingPanel.getTagButtons().size() > 0) {
            add(tagButtonPanel);
            taggingPanel.refreshFooterPanel();
        } else {
            remove(tagButtonPanel);
            taggingPanel.refreshFooterPanel();
        }
    }

    /**
     * If the height of the tagButtonPanel has changed after deleting (delta not equals 0 ), the footerWidget of the
     * TaggingPanel has a different height, which in this case might cause the contentWidget to be to small.
     */
    protected void recalculateHeight() {
        tagButtonPanel.recalculateHeight();
    }

    /**
     * Loads all tag buttons from user storage and displays them.
     */
    protected void loadAllTagButtons() {
        tagButtonPanel.loadAllTagButtons();
    }

    /**
     * Stores all tag buttons into user storage.
     */
    protected void storeAllTagButtons() {
        tagButtonPanel.storeAllTagButtons();
    }
}
