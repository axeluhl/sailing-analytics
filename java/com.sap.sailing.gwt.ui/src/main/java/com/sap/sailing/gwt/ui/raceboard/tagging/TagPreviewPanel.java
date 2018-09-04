package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel showing {@link TagCell} as preview used at {@link TagButtonDialog}.
 */
public class TagPreviewPanel extends FlowPanel {

    private final TagCellListResources cellResources = TagCellListResources.INSTANCE;
    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final UserService userService;

    private final CellList<TagDTO> tagPreviewCellList;
    private List<TagDTO> listContainingPreviewTag;

    /**
     * Creates panel showing preview of tag when creating a {@link TagButton}. Adds change listener on input fields to
     * be able to rerender the {@link TagCell preview cell} whenever user changes values of the input fields.
     * 
     * @param taggingPanel
     *            references {@link UserService}
     * @param inputPanel
     *            references input fields used to get current user input
     */
    protected TagPreviewPanel(TaggingPanel taggingPanel, TagInputPanel inputPanel) {
        this.taggingPanel = taggingPanel;
        this.userService = taggingPanel.getUserSerivce();

        setStyleName(style.tagPreviewPanel());

        tagPreviewCellList = new CellList<TagDTO>(new TagCell(taggingPanel, true), cellResources);
        listContainingPreviewTag = new ArrayList<TagDTO>();

        add(new Label(taggingPanel.getStringMessages().tagPreview()));
        add(tagPreviewCellList);

        inputPanel.getTagTextBox().addValueChangeHandler(event -> {
            renderPreview(inputPanel);
        });
        inputPanel.getImageURLTextBox().addValueChangeHandler(event -> {
            renderPreview(inputPanel);
        });
        inputPanel.getCommentTextArea().addValueChangeHandler(event -> {
            renderPreview(inputPanel);
        });
        inputPanel.getVisibleForPublicCheckBox().addValueChangeHandler(event -> {
            renderPreview(inputPanel);
        });
        renderPreview(inputPanel);
    }

    /**
     * Renders preview of tag by getting data from the input fields.
     * 
     * @param inputPanel
     *            used to get input values from user
     */
    protected void renderPreview(TagInputPanel inputPanel) {
        listContainingPreviewTag.removeAll(listContainingPreviewTag);
        listContainingPreviewTag.add(new TagDTO(inputPanel.getTag(), inputPanel.getComment(), inputPanel.getImageURL(),
                userService.getCurrentUser().getName(), inputPanel.isVisibleForPublic(),
                new MillisecondsTimePoint(taggingPanel.getTimerTime()), MillisecondsTimePoint.now()));
        tagPreviewCellList.setRowData(listContainingPreviewTag);

        setVisible(!inputPanel.getTag().isEmpty());
    }
}
