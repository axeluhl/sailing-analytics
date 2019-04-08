package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.adminconsole.ColorColumn.ColorRetriever;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.common.Color;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

public class MarkTableWrapper<S extends RefreshableSelectionModel<MarkDTO>> extends TableWrapper<MarkDTO, S> {    
    public MarkTableWrapper(boolean multiSelection, SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, multiSelection, true,
                new EntityIdentityComparator<MarkDTO>() {
                    @Override
                    public boolean representSameEntity(MarkDTO dto1, MarkDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(MarkDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
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
                return mark.position.getLatDeg() + ", " + mark.position.getLngDeg();
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
                return t.color;
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
    
    public void refresh(Iterable<MarkDTO> marks) {
        super.refresh(marks);
        Collections.sort(dataProvider.getList(), new Comparator<MarkDTO>() {
            @Override
            public int compare(MarkDTO o1, MarkDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
}
