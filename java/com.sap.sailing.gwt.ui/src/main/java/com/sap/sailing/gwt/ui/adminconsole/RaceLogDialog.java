package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;

public class RaceLogDialog extends AbstractLogDialog<RaceLogDTO, RaceLogEventDTO> {
    private final StringMessages stringMessages;

    public RaceLogDialog(final RaceLogDTO raceLogDTO, final StringMessages stringMessages, DialogCallback<RaceLogDTO> callback) {
        super(raceLogDTO, stringMessages, stringMessages.raceLog(), callback);
        this.stringMessages = stringMessages;
    }

    @Override
    protected void addFirstColumns(CellTable<RaceLogEventDTO> table, ListHandler<RaceLogEventDTO> columnSortHandler) {
        TextColumn<RaceLogEventDTO> raceLogEventPassIdColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                return String.valueOf(raceLogEventDTO.getPassId());
            }
        };
        raceLogEventPassIdColumn.setSortable(true);
        columnSortHandler.setComparator(raceLogEventPassIdColumn, new Comparator<RaceLogEventDTO>() {
            @Override
            public int compare(RaceLogEventDTO r1, RaceLogEventDTO r2) {
                return (r1.getPassId()<r2.getPassId() ? -1 : (r1.getPassId()==r2.getPassId() ? 0 : 1));
            }
        });
        table.addColumn(raceLogEventPassIdColumn, "PassId");
    }

    @Override
    protected void addFirstWidgetComponents(VerticalPanel vPanel) {
        Label currentPassLabel = new Label(stringMessages.currentPass() + ":" + getLogDTO().getCurrentPassId());
        vPanel.add(currentPassLabel);
    }
}
