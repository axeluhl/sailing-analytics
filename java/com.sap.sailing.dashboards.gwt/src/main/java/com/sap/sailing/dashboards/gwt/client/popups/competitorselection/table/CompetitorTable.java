package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.table;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;
import com.sap.sse.gwt.client.celltable.BaseCelltable;

public class CompetitorTable extends AbsolutePanel {

    private CellTable<CompetitorWithoutBoatDTO> table;
    private CompetitorTableRowSelectionListener competitorTableRowSelectionListener;

    public CompetitorTable(CompetitorTableRowSelectionListener competitorTableRowSelectionListener) {
        this.competitorTableRowSelectionListener = competitorTableRowSelectionListener;
    }

    public void setTableContent(List<CompetitorWithoutBoatDTO> competitorNames) {
        if (table == null) {
            initTable(competitorNames);
        }
    }

    private void initTable(List<CompetitorWithoutBoatDTO> competitorNames) {
        CellTable.Resources tableRes = GWT.create(CompetitorTableStyleResource.class);

        table = new BaseCelltable<CompetitorWithoutBoatDTO>(15, tableRes);
        table.getElement().getStyle().setWidth(100, Unit.PCT);
        table.setSkipRowHoverStyleUpdate(true);
        table.setSkipRowHoverCheck(true);
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        final SingleSelectionModel<CompetitorWithoutBoatDTO> selectionModel = new SingleSelectionModel<CompetitorWithoutBoatDTO>();
        table.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                CompetitorWithoutBoatDTO selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    competitorTableRowSelectionListener.didSelectedRowWithCompetitorName(selected);
                }
            }
        });

        TextColumn<CompetitorWithoutBoatDTO> competitorNameCollumn = new TextColumn<CompetitorWithoutBoatDTO>() {
            @Override
            public String getValue(CompetitorWithoutBoatDTO competitor) {
                return competitor.getName();
            }
        };
        table.addColumn(competitorNameCollumn, "");
        table.setColumnWidth(competitorNameCollumn, 25.0, Unit.PCT);
        table.setRowCount(competitorNames.size(), true);
        table.setRowData(competitorNames);
        this.add(table);
    }
}
