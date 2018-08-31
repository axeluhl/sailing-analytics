package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.raceboard.tagging.TagPanelResources.TagPanelStyle;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.ui.client.UserService;

/**
 * Panel used to preview a tag.
 */
public class TagPreviewPanel extends FlowPanel {

    private final TagCellListResources cellResources = TagCellListResources.INSTANCE;
    private final TagPanelStyle style = TagPanelResources.INSTANCE.style();

    private final TaggingPanel taggingPanel;
    private final UserService userService;

    private final CellList<TagDTO> tagPreviewCellList;
    private List<TagDTO> listContainingPreviewTag;

    public TagPreviewPanel(TaggingPanel taggingPanel, TagInputPanel inputPanel) {
        this.taggingPanel = taggingPanel;
        this.userService = taggingPanel.getUserSerivce();

        setStyleName(style.tagPreviewPanel());

        tagPreviewCellList = new CellList<TagDTO>(new TagCell(taggingPanel, true), cellResources);
        listContainingPreviewTag = new ArrayList<TagDTO>();

        add(new Label(taggingPanel.getStringMessages().tagPreview()));
        add(tagPreviewCellList);

        inputPanel.getTagTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                renderPreview(inputPanel);
            }

        });
        inputPanel.getImageURLTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                renderPreview(inputPanel);
            }

        });
        inputPanel.getCommentTextArea().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                renderPreview(inputPanel);
            }

        });
        inputPanel.getVisibleForPublicCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                renderPreview(inputPanel);
            }
        });
        renderPreview(inputPanel);
    }

    public void renderPreview(TagInputPanel inputPanel) {
        listContainingPreviewTag.removeAll(listContainingPreviewTag);
        listContainingPreviewTag.add(new TagDTO(inputPanel.getTag(), inputPanel.getComment(), inputPanel.getImageURL(),
                userService.getCurrentUser().getName(), inputPanel.isVisibleForPublic(),
                new MillisecondsTimePoint(taggingPanel.getTimerTime()), MillisecondsTimePoint.now()));
        tagPreviewCellList.setRowData(listContainingPreviewTag);

        setVisible(!inputPanel.getTag().isEmpty());
    }
}
