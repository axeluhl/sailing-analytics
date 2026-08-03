package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CourseAreaDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CourseAreaSelectionDialog extends DataEntryDialog<List<CourseAreaDTO>> {
    private final List<CourseAreaDTO> courseAreas;
    private final StringMessages stringMessages;
    private Grid selectionGrid;

    public CourseAreaSelectionDialog(final List<CourseAreaDTO> courseAreas, final StringMessages stringMessages,
            final DialogCallback<List<CourseAreaDTO>> callback) {
        super(stringMessages.selectCourseAreas(), null, stringMessages.ok(), stringMessages.cancel(), null, callback);
        this.courseAreas = courseAreas;
        this.stringMessages = stringMessages;
    }

    @Override
    protected List<CourseAreaDTO> getResult() {
        final List<CourseAreaDTO> selected = new ArrayList<>();
        for (int i = 0; i < courseAreas.size(); i++) {
            final CheckBox checkBox = (CheckBox) selectionGrid.getWidget(i, 1);
            if (checkBox.getValue()) {
                selected.add(courseAreas.get(i));
            }
        }
        return selected;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(5);
        final HorizontalPanel multiSelectPanel = new HorizontalPanel();
        multiSelectPanel.setSpacing(3);
        final Button selectAllButton = new Button(stringMessages.selectAll());
        selectAllButton.addClickHandler(createMultiSelectionHandler(true));
        final Button deselectAllButton = new Button(stringMessages.deselectAll());
        deselectAllButton.addClickHandler(createMultiSelectionHandler(false));
        multiSelectPanel.add(selectAllButton);
        multiSelectPanel.add(deselectAllButton);
        mainPanel.add(multiSelectPanel);
        selectionGrid = new Grid(courseAreas.size(), 2);
        selectionGrid.setCellSpacing(5);
        for (int i = 0; i < courseAreas.size(); i++) {
            selectionGrid.setWidget(i, 0, new Label(courseAreas.get(i).getName()));
            selectionGrid.setWidget(i, 1, new CheckBox());
        }
        mainPanel.add(selectionGrid);
        return mainPanel;
    }

    private ClickHandler createMultiSelectionHandler(final boolean checked) {
        return new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                for (int i = 0; i < selectionGrid.getRowCount(); i++) {
                    ((CheckBox) selectionGrid.getWidget(i, 1)).setValue(checked);
                }
            }
        };
    }
}
