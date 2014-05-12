package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.impl.RGBColor;
import com.sap.sailing.gwt.ui.adminconsole.ColorColumn.ColorRetriever;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class MarkTableWrapper<S extends SelectionModel<MarkDTO>> extends TableWrapper<MarkDTO, S> {    
    public MarkTableWrapper(S selectionModel, SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, selectionModel);

        mainPanel.add(table);
        
        TextColumn<MarkDTO> markNameColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getName();
            }
        };
        table.addColumn(markNameColumn, stringMessages.mark());

        final SafeHtmlCell markPositionCell = new SafeHtmlCell();
        Column<MarkDTO, SafeHtml> markPositionColumn = new Column<MarkDTO, SafeHtml>(markPositionCell) {
            @Override
            public SafeHtml getValue(MarkDTO mark) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                if(mark.position != null) {
                    NumberFormat fmt = NumberFormat.getFormat("#.###");
                    builder.appendEscaped(fmt.format(mark.position.latDeg)+", "+fmt.format(mark.position.lngDeg));
                }
                return builder.toSafeHtml();
            }
        };
        table.addColumn(markPositionColumn, stringMessages.position());
        
        Column<MarkDTO, SafeHtml> markColorColumn = new ColorColumn<>(new ColorRetriever<MarkDTO>() {
            @Override
            public Color getColor(MarkDTO t) {
                return t.color != null && ! t.color.isEmpty() ? new RGBColor(t.color) : null;
            }
        });
        table.addColumn(markColorColumn, stringMessages.color());

        TextColumn<MarkDTO> markShapeColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.shape != null ? markDTO.shape : "";
            }
        };
        table.addColumn(markShapeColumn, stringMessages.shape());

        TextColumn<MarkDTO> markPatternColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.pattern != null ? markDTO.pattern : "";
            }
        };
        table.addColumn(markPatternColumn, stringMessages.pattern());

        TextColumn<MarkDTO> markUUIDColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getIdAsString();
            }
        };
        table.addColumn(markUUIDColumn, "UUID");
    }
    
    public void refresh(Collection<MarkDTO> marks) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(marks);
        dataProvider.flush();
        Collections.sort(dataProvider.getList(), new Comparator<MarkDTO>() {
            @Override
            public int compare(MarkDTO o1, MarkDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
    
    public void refresh(String leaderboardName, String raceColumnName, String fleetName) {
        sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<MarkDTO>>() {
            @Override
            public void onSuccess(Collection<MarkDTO> result) {
                refresh(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load marks: " + caught.getMessage());
            }
        });
    }
}
