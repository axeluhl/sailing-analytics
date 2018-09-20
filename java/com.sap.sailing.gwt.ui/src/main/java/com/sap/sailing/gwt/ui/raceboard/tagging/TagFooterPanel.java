package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanel.State;

/**
 * Panel containg {@link TagModificationPanel} and {@link TagButtonPanel} which is used as
 * {@link TaggingPanel#footerPanel footer} in {@link TaggingPanel}.
 */
public class TagFooterPanel extends FlowPanel {

    private final TaggingPanel taggingPanel;

    private final TagModificationPanel tagModificationPanel;
    private final TagButtonPanel tagButtonPanel;

    /**
     * Creates footer panel and loads {@link TagButton tag-buttons} from {@link com.sap.sse.security.UserStore
     * UserStore}.
     * 
     * @param taggingPanel
     *            required to instantiate {@link TagModificationPanel} and {@link TagButtonPanel}
     */
    protected TagFooterPanel(TaggingPanel taggingPanel) {
        this.taggingPanel = taggingPanel;

        tagModificationPanel = new TagModificationPanel(taggingPanel, this);
        tagButtonPanel = new TagButtonPanel(taggingPanel, this);

        // Tag-buttons are only shown if amount of tag-buttons is greater then 0!
        setTagButtonsVisibility(true);

        // input fields are hidden by default
        setInputFieldsVisibility(false);

        tagButtonPanel.loadAllTagButtons();
    }

    /**
     * Sets visibility of {@link TagModificationPanel input fields} for new tag and {@link TaggingPanel#createTagsButton
     * "Edit Tag-Buttons"-button}. {@link TagModificationPanel Input fields} will not be hidden but removed completly
     * from DOM in case <code>visible</code> is set to <code>false</code>!
     * 
     * @param visible
     *            should be <code>true</code> when user is logged in and {@link TaggingPanel} is in
     *            {@link TaggingPanel#currentState EDIT-mode}, otherwise <code>false</code>
     */
    protected void setInputFieldsVisibility(boolean visible) {
        if (visible) {
            add(tagModificationPanel);
        } else {
            remove(tagModificationPanel);
        }
    }

    /**
     * Sets visibility of {@link TagButtonPanel tagButtonPanel}. {@link TagButtonPanel tagButtonPanel} will only be
     * shown if amount of {@link TaggingPanel#getTagButtons() tag-buttons} is greater then <code>0</code>!
     * 
     * @param visible
     *            should be <code>true</code> when user is logged in, otherwise <code>false</code>
     */
    protected void setTagButtonsVisibility(boolean visible) {
        if (visible && taggingPanel.getTagButtons().size() > 0) {
            add(tagButtonPanel);
        } else {
            remove(tagButtonPanel);
        }
    }

    protected void setCurrentState(State state) {
        if (state == State.CREATE_TAG) {
            setInputFieldsVisibility(true);
            setTagButtonsVisibility(true);
            tagModificationPanel.setCurrentStatus(state);
        } else if (state == State.EDIT_TAG) {
            setInputFieldsVisibility(true);
            setTagButtonsVisibility(false);
            tagModificationPanel.setCurrentStatus(state);
        } else {
            setInputFieldsVisibility(false);
            setTagButtonsVisibility(true);
            tagModificationPanel.setCurrentStatus(state);
        }
    }

    /**
     * If height of the {@link TagButtonPanel tagButtonPanel} has changed after deleting (delta height does not equal
     * 0), the {@link TaggingPanel#footerPanel footer widget} of the {@link TaggingPanel} has a different height, which
     * in this case might cause the {@link TaggingPanel#contentPanel content widget} to be to small.
     */
    protected void recalculateHeight() {
        tagButtonPanel.recalculateHeight();
    }

    /**
     * Loads all {@link TagButton tag-buttons} from {@link com.sap.sse.security.UserStore UserStore} and displays them.
     */
    protected void loadAllTagButtons() {
        tagButtonPanel.loadAllTagButtons();
    }

    /**
     * Stores all {@link TagButton tag-buttons} into {@link com.sap.sse.security.UserStore UserStore}.
     */
    protected void storeAllTagButtons() {
        tagButtonPanel.storeAllTagButtons();
    }
}
