package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class CompetitorTable extends AbsolutePanel {

    private CellTable<String> table;
    private CompetitorTableRowSelectionListener competitorTableRowSelectionListener;

    public CompetitorTable(CompetitorTableRowSelectionListener competitorTableRowSelectionListener) {
        this.competitorTableRowSelectionListener = competitorTableRowSelectionListener;
    }

    public void setTableContent(List<String> competitorNames) {
        if (table == null) {
            initTable(competitorNames);
        }
    }

    private void initTable(List<String> competitorNames) {
        CellTable.Resources tableRes = GWT.create(CompetitorTableStyleResource.class);

        table = new CellTable<String>(15, tableRes);
        table.getElement().getStyle().setWidth(100, Unit.PCT);
        table.setSkipRowHoverStyleUpdate(true);
        table.setSkipRowHoverCheck(true);
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
        table.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                String selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    competitorTableRowSelectionListener.didSelectedRowWithCompetitorName(selected);
                }
            }
        });

        TextColumn<String> competitorNameCollumn = new TextColumn<String>() {
            @Override
            public String getValue(String competitorName) {
                return competitorName;
            }
        };
        table.addColumn(competitorNameCollumn, "");
        table.setColumnWidth(competitorNameCollumn, 25.0, Unit.PCT);
        table.setRowCount(competitorNames.size(), true);
        table.setRowData(competitorNames);
        this.add(table);
    }
}
