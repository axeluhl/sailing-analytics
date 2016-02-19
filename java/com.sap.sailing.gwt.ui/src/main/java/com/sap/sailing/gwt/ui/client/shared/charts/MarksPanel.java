package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPositionPanel.NotificationType;
import com.sap.sailing.gwt.ui.client.shared.controls.FlushableSortedCellTableWithStylableHeaders;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardTableResources;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class MarksPanel extends SimplePanel implements Component<AbstractSettings> {
    private static final LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);
    
    private final ListDataProvider<MarkDTO> markDataProvider;    
    private final FlushableSortedCellTableWithStylableHeaders<MarkDTO> markTable;
    
    public MarksPanel(final EditMarkPositionPanel parent, final ListDataProvider<MarkDTO> markDataProvider, final StringMessages stringMessages) {
        this.markDataProvider = markDataProvider;
        setTitle("Marks");
        markTable = new FlushableSortedCellTableWithStylableHeaders<MarkDTO>(10000, tableResources);
        markTable.setStyleName("EditMarkPositionMarkTable");
        SelectionCheckboxColumn<MarkDTO> selectionCheckboxColumn = new SelectionCheckboxColumn<MarkDTO>(
                tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(),
                tableResources.cellTableStyle().cellTableCheckboxColumnCell(),
                new EntityIdentityComparator<MarkDTO>() {
                    @Override
                    public boolean representSameEntity(MarkDTO dto1, MarkDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(MarkDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                },
                this.markDataProvider,
                markTable);
        markTable.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        markTable.setColumnWidth(selectionCheckboxColumn, 27, Unit.PX);
        SortableColumn<MarkDTO, String> markNameColumn = new SortableColumn<MarkDTO, String>(new TextCell(), SortingOrder.NONE) {
            @Override
            public String getValue(MarkDTO object) {
                return object.getName();
            }

            @Override
            public InvertibleComparator<MarkDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return null;
            }
        };
        markTable.addColumn(markNameColumn, new TextHeader(stringMessages.marks()));
        SortableColumn<MarkDTO, String> addFixColumn = new SortableColumn<MarkDTO, String>(new ButtonCell(), SortingOrder.NONE) {
            @Override
            public String getValue(MarkDTO object) {
                return stringMessages.addNewFix();
            }

            @Override
            public InvertibleComparator<MarkDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return null;
            }
        };
        addFixColumn.setFieldUpdater(new FieldUpdater<MarkDTO, String>() {
            @Override
            public void update(int index, final MarkDTO mark, String value) {
                if (parent.hasFixAtTimePoint(mark)) {
                    parent.showNotification(stringMessages.pleaseSelectOtherTimepoint(), NotificationType.ERROR);
                } else {
                    parent.createFixPositionChooserToAddFixToMark(mark, new Callback<Position, Exception>() {
                        @Override
                        public void onFailure(Exception reason) {
                            parent.resetCurrentFixPositionChooser();
                        }
                        @Override
                        public void onSuccess(Position result) {
                            parent.addMarkFix(mark, parent.timer.getTime(), result);
                            parent.resetCurrentFixPositionChooser();
                        }
                    });
                }
            }
        });
        markTable.addColumn(addFixColumn, new TextHeader(""));
        markTable.setSelectionModel(selectionCheckboxColumn.getSelectionModel(), selectionCheckboxColumn.getSelectionManager());
        markTable.getSelectionModel().addSelectionChangeHandler(parent);
        markDataProvider.addDataDisplay(markTable);
        setWidget(markTable);
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
    
    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    public List<MarkDTO> getSelectedMarks() {
        List<MarkDTO> list = new ArrayList<MarkDTO>();
        for (MarkDTO mark : markDataProvider.getList()) {
            if (markTable.getSelectionModel().isSelected(mark)) {
                list.add(mark);
            }
        }
        return list;               
    }
    
    public void deselectMark(MarkDTO mark) {
        markTable.getSelectionModel().setSelected(mark, false);
    }

    public void deselectMarks() {
        for (MarkDTO mark : markDataProvider.getList()) {
            deselectMark(mark);
        }
    }

    public void select(MarkDTO mark) {
        markTable.getSelectionModel().setSelected(mark, true);
    }
}