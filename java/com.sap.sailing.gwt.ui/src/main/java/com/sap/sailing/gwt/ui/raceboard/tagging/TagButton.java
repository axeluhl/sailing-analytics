package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.io.Serializable;

import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;

/**
 * Used to store tag button data and creates new tag event when clicking the button.
 */
public class TagButton extends Button implements Serializable {

    private static final long serialVersionUID = -722157125410637316L;

    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private String tag, imageURL, comment;
    private boolean visibleForPublic;

    public TagButton(String buttonName, String tag, String imageURL, String comment,
            boolean visibleForPublic) {
        super(buttonName);
        setStyleName(style.tagDialogButton());

        this.tag = tag;
        this.imageURL = imageURL;
        this.comment = comment;
        this.visibleForPublic = visibleForPublic;

        addStyleName("gwt-Button");
    }

    public String getTag() {
        return tag;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getComment() {
        return comment;
    }

    public boolean isVisibleForPublic() {
        return visibleForPublic;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setVisibleForPublic(boolean visibleForPublic) {
        this.visibleForPublic = visibleForPublic;
    }
}