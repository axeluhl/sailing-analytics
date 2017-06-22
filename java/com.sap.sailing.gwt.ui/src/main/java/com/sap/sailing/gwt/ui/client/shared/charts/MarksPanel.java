package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.Date;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.EditMarkPositionPanel.NotificationType;
import com.sap.sailing.gwt.ui.client.shared.controls.FlushableSortedCellTableWithStylableHeaders;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardTableResources;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class MarksPanel extends AbstractCompositeComponent<AbstractSettings> {
    private static final LeaderboardTableResources tableResources = GWT.create(LeaderboardTableResources.class);
    
    private final ListDataProvider<MarkDTO> markDataProvider;    
    private final FlushableSortedCellTableWithStylableHeaders<MarkDTO> markTable;
    
    public MarksPanel(final EditMarkPositionPanel parent, ComponentContext<?> context,
            final ListDataProvider<MarkDTO> markDataProvider, final StringMessages stringMessages) {
        super(parent, context);
        this.markDataProvider = markDataProvider;
        markTable = new FlushableSortedCellTableWithStylableHeaders<MarkDTO>(10000, tableResources);
        markTable.setStyleName("EditMarkPositionMarkTable");
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
                final Date timePoint = parent.timer.getTime();
                select(mark);
                if (parent.hasFixAtTimePoint(mark, timePoint)) {
                    parent.showNotification(stringMessages.pleaseSelectOtherTimepoint(), NotificationType.ERROR);
                } else {
                    parent.createFixPositionChooserToAddFixToMark(mark, new Callback<Position, Exception>() {
                        @Override
                        public void onFailure(Exception reason) {
                            parent.resetCurrentFixPositionChooser();
                        }
                        @Override
                        public void onSuccess(Position result) {
                            parent.addMarkFix(mark, timePoint, result);
                            parent.resetCurrentFixPositionChooser();
                        }
                    });
                }
            }
        });
        markTable.addColumn(addFixColumn, new TextHeader(""));
        SingleSelectionModel<MarkDTO> selectionModel = new RefreshableSingleSelectionModel<MarkDTO>(new EntityIdentityComparator<MarkDTO>() {
            @Override
            public boolean representSameEntity(MarkDTO dto1, MarkDTO dto2) {
                return dto1.getIdAsString().equals(dto2.getIdAsString());
            }
            @Override
            public int hashCode(MarkDTO t) {
                return t.getIdAsString().hashCode();
            }
        },this.markDataProvider);
        markTable.setSelectionModel(selectionModel);
        markTable.getSelectionModel().addSelectionChangeHandler(parent);
        markDataProvider.addDataDisplay(markTable);
        initWidget(markTable);
        setTitle(stringMessages.marks());
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
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent(AbstractSettings settings) {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    public MarkDTO getSelectedMark() {
        for (MarkDTO mark : markDataProvider.getList()) {
            if (markTable.getSelectionModel().isSelected(mark)) {
               	return mark;
            }
        }
        return null;               
    }
    
    public void deselectMark() {
        markTable.getSelectionModel().setSelected(getSelectedMark(), false);
    }

    public void select(MarkDTO mark) {
        markTable.getSelectionModel().setSelected(mark, true);
    }
    
    @Override
    public AbstractSettings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "MarksPanel";
    }

}