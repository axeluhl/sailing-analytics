package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.gwt.ui.raceboard.tagging.TaggingPanelResources.TagPanelStyle;

/**
 * Used to store preset {@link com.sap.sailing.gwt.ui.shared.TagDTO tags} which will be rendered in {@link TaggingPanel}
 * as buttons for quick and easy access. Clicking on tag-buttons does not have any effect until click listener gets
 * added manually! See example at {@link TaggingPanel#addTagButton(TagButton) addTagButton()}.
 */
public class TagButton extends Button {

    private final TagPanelStyle style = TaggingPanelResources.INSTANCE.style();

    private String tag, imageURL, comment;
    private boolean visibleForPublic;

    /**
     * Creates tag button with given attributes.
     * 
     * @param buttonName
     *            Usually equal with title
     * @param tag
     *            title of corresponding tag
     * @param imageURL
     *            URL to optional image of corresponding tag, may be <code>null</code> in case of missing image
     * @param comment
     *            optional comment of corresponding tag, may be <code>null</code> in case of missing comment
     * @param visibleForPublic
     *            should be <code>true</code> if everybody should see the generated tag, otherwise <code>false</code>
     */
    protected TagButton(String buttonName, String tag, String imageURL, String comment, boolean visibleForPublic) {
        super(buttonName);
        setStyleName(style.tagDialogButton());

        this.tag = tag;
        this.imageURL = imageURL;
        this.comment = comment;
        this.visibleForPublic = visibleForPublic;

        addStyleName("gwt-Button");
    }

    /**
     * Returns title of corresponding tag.
     * 
     * @return title of tag
     */
    protected String getTag() {
        return tag;
    }

    /**
     * Returns image URL of corresponding tag.
     * 
     * @return image URL of tag
     */
    protected String getImageURL() {
        return imageURL;
    }

    /**
     * Returns comment of corresponding tag.
     * 
     * @return comment of tag
     */
    protected String getComment() {
        return comment;
    }

    /**
     * Returns visibility of corresponding tag.
     * 
     * @return <code>true</code> if tag is visible for public, otherwise <code>false</code>
     */
    protected boolean isVisibleForPublic() {
        return visibleForPublic;
    }

    /**
     * Sets title of corresponding tag.
     * 
     * @param tag
     *            title, must not be <code>null</code>
     */
    protected void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Sets optional image URL of corresponding tag.
     * 
     * @param imageURL
     *            image URL, may be <code>null</code>
     */
    protected void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * Sets optional comment of corresponding tag.
     * 
     * @param comment
     *            comment, may be <code>null</code>
     */
    protected void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets visibility of corresponding tag.
     * 
     * @param visibleForPublic
     *            should be <code>true</code> if tag is visible for public, otherwise <code>false</code>
     */
    protected void setVisibleForPublic(boolean visibleForPublic) {
        this.visibleForPublic = visibleForPublic;
    }
}