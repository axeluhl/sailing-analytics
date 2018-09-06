package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Interface for styling {@link TaggingPanel} and its content.
 */
public interface TagPanelResources extends ClientBundle {
    public static final TagPanelResources INSTANCE = GWT.create(TagPanelResources.class);

    @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
    ImageResource editIcon();

    @Source("com/sap/sse/gwt/client/images/remove.png")
    ImageResource deleteIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/lock.png")
    ImageResource privateIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/unlock.png")
    ImageResource publicIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Clear.png")
    ImageResource clearButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_CompetitorsFilter_INACTIVE.png")
    ImageResource filterInactiveButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_CompetitorsFilter_ACTIVE.png")
    ImageResource filterActiveButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Search.png")
    ImageResource searchButton();

    @Source("com/sap/sailing/gwt/ui/client/images/SAP_RV_Settings.png")
    ImageResource settingsButton();

    @Source("com/sap/sailing/gwt/ui/client/images/plus_transparent.png")
    ImageResource plusTransparent();

    @Source("tagging-panel.gss")
    public TagPanelStyle style();

    public interface TagPanelStyle extends CssResource {
        // general
        String hidden();
        String taggingPanel();
        String taggingPanelDisabled();
        String toggleEditState();
        String buttonsPanel();
        String confirmationDialog();
        String confirmationDialogPanel();

        // tag cells
        String tagCell();
        String tagCellHeading();
        String tagCellHeadingButtons();
        String tagActionButton();
        String tagEditButton();
        String tagDeleteButton();
        String tagCellCreated();
        String tagCellComment();
        String tagCellImage();
        String tagCellListPanel();

        // tag-buttons
        String tagButtonDialog(); // dialog itself
        String tagButtonDialogPanel();
        String tagDialogButton(); // button in dialog
        String tagButtonTable();
        String tagButtonTableWrapper();
        String tagPreviewPanel();
        String tagButtonPanel();
        String tagButtonPanelHeader();

        // tag input / creation
        String tagModificationPanel();
        String tagModificationPanelHeader();
        String tagModificationPanelHeaderLabel();
        String tagModificationPanelHeaderButton();
        String tagInputPanel();
        String tagInputPanelTag();
        String tagInputPanelComment();
        String tagInputPanelImageURL();
        String tagInputPanelIsVisibleForPublic();

        // tag filtering
        String tagFilterButton();
        String tagFilterHiddenButton();
        String tagFilterClearButton();
        String tagFilterSearchButton();
        String tagFilterSettingsButton();
        String tagFilterFilterButton();
        String tagFilterPanel();
        String tagFilterSearchBox();
        String tagFilterSearchInput();
        String tagFilterCurrentSelection();

        // images
        String imageActiveFilter();
        String imageInactiveFilter();
        String imageSearch();
        String imageClearSearch();
        String imageSettings();
        String imagePusTransparent();
        String imageEdit();
        String imageDelete();
    }
}
