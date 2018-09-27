package com.sap.sailing.gwt.ui.raceboard.tagging;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * Interface for styling {@link TaggingPanel} and its content.
 */
public interface TaggingPanelResources extends CellList.Resources, CellTable.Resources {
    static final TaggingPanelResources INSTANCE = GWT.create(TaggingPanelResources.class);

    @Source("com/sap/sailing/gwt/ui/client/images/share.png")
    ImageResource shareIcon();
    
    @Source("com/sap/sailing/gwt/ui/client/images/edit.png")
    ImageResource editIcon();

    @Source("com/sap/sse/gwt/client/images/remove.png")
    ImageResource deleteIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/timeslider-playstate-replay-active.png")
    ImageResource reloadIcon();

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

    @Override
    @Source("tag-button-celltable.gss")
    CellTable.Style cellTableStyle();

    @Override
    @Source("tag-celllist.gss")
    CellList.Style cellListStyle();

    @Source("tagging-panel.gss")
    TagPanelStyle style();

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
        String tagCellActive();
        String tagCellHeading();
        String tagCellHeadingButtons();
        String tagActionButton();
        String tagEditButton();
        String tagDeleteButton();
        String tagShareButton();
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
        String tagInputPanelImage();
        String tagInputPanelImageTextBox();
        String tagInputPanelImageButton();
        String tagInputPanelImageFormPanel();
        String tagInputPanelIsVisibleForPublic();
        String tagInputPanelNoPermissionLabel();

        // tag filtering
        String tagFilterButton();
        String tagFilterHiddenButton();
        String tagFilterClearButton();
        String tagFilterSearchButton();
        String tagFilterSettingsButton();
        String tagReloadButton();
        String tagFilterPanel();
        String tagFilterSearchBox();
        String tagFilterSearchInput();
        String tagFilterCurrentSelection();
        
        //shared tag dialog
        String tagSharedURLLabel();
        String tagSharedURLTextBox();
        String tagSharedURLDialog();

        // images
        String imageActiveFilter();
        String imageInactiveFilter();
        String imageSearch();
        String imageClearSearch();
        String imageSettings();
        String imagePusTransparent();
        String imageShare();
        String imageEdit();
        String imageDelete();
        String imageReload();
    }
}
