package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;

/**
 * Used to store preset {@link com.sap.sailing.gwt.ui.shared.TagDTO tags} which will be rendered in {@link TaggingPanel}
 * as buttons for quick and easy access. Clicking on tag-buttons does not have any effect until click listener gets
 * added manually! See example at {@link TaggingPanel#addTagButton(TagButton) addTagButton()}.
 */
public class TagButton extends Button{

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private String tag, imageURL, comment;
    private boolean visibleForPublic;
    private int imageWidth, imageHeight;

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
    protected TagButton(String buttonName, String tag, String imageURL, int imageWidth, int imageHeight, String comment, boolean visibleForPublic) {
        super(buttonName);
        setStyleName(style.tagDialogButton());

        this.tag = tag;
        this.imageURL = imageURL;
        this.comment = comment;
        this.visibleForPublic = visibleForPublic;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
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
     * Returns image width of corresponding tag.
     * 
     * @return image width of tag
     */
    protected int getImageWidth() {
        return imageWidth;
    }

    /**
     * Returns image height of corresponding tag.
     * 
     * @return image height of tag
     */
    protected int getImageHeight() {
        return imageHeight;
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
     * Sets URL, width and height of optional image for corresponding tag.
     * 
     * @param imageURL
     *            image URL, may be <code>null</code>
     */
    protected void setImage(String imageURL, int imageWidth, int imageHeight) {
        this.imageURL = imageURL;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
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