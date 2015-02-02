package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.gwt.ui.adminconsole.ColorColumn.ColorRetriever;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.client.ErrorReporter;

public class MarkTableWrapper<S extends SelectionModel<MarkDTO>> extends TableWrapper<MarkDTO, S> {    
    public MarkTableWrapper(S selectionModel, SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, selectionModel, true);
        
        TextColumn<MarkDTO> markNameColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getName();
            }
        };
        table.addColumn(markNameColumn, stringMessages.mark());

        TextColumn<MarkDTO> markPositionColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO mark) {
                if (mark.position == null) {
                    return "";
                }
                return mark.position.latDeg + ", " + mark.position.lngDeg;
            }
        };
        table.addColumn(markPositionColumn, stringMessages.position());

        TextColumn<MarkDTO> markTypeColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO mark) {
                return mark.type == null ? "" : mark.type.toString();
            }
        };
        table.addColumn(markTypeColumn, stringMessages.type());
        
        Column<MarkDTO, SafeHtml> markColorColumn = new ColorColumn<>(new ColorRetriever<MarkDTO>() {
            @Override
            public Color getColor(MarkDTO t) {
                Color result;
                try {
                    result = t.color != null && !t.color.isEmpty() ? AbstractColor
                            .getColorByLowercaseNameStatic(t.color.toLowerCase()) == null ? new RGBColor(t.color)
                            : AbstractColor.getColorByLowercaseNameStatic(t.color.toLowerCase()) : null;
                } catch (IllegalArgumentException e) {
                    // if the color name isn't recognized, neither as a named color nor as a HTML color code, an exception will
                    // result because of the attempt to interpret something unknown as an HTML color. Catch this and return null as
                    // the color
                    result = null;
                }
                return result;
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
