package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RemoteServerInstancesManagementTableWrapper extends
        TableWrapper<RemoteSailingServerReferenceDTO, RefreshableMultiSelectionModel<RemoteSailingServerReferenceDTO>, StringMessages, CellTableWithCheckboxResources> {

    public RemoteServerInstancesManagementTableWrapper(StringMessages stringMessages, ErrorReporter errorReporter,
            ListDataProvider<RemoteSailingServerReferenceDTO> dataProvider) {
        super(stringMessages, errorReporter, true, true,
                new EntityIdentityComparator<RemoteSailingServerReferenceDTO>() {
                    @Override
                    public boolean representSameEntity(RemoteSailingServerReferenceDTO dto1,
                            RemoteSailingServerReferenceDTO dto2) {
                        return dto1.getUrl().equals(dto2.getUrl());
                    }

                    @Override
                    public int hashCode(RemoteSailingServerReferenceDTO t) {
                        return t.getUrl().hashCode();
                    }
                });
        registerSelectionModelOnNewDataProvider(dataProvider);
    }

    @Override
    public FlushableCellTable<RemoteSailingServerReferenceDTO> getTable() {
        return (FlushableCellTable<RemoteSailingServerReferenceDTO>) super.getTable();
    }

    public <T> void addColumn(Column<RemoteSailingServerReferenceDTO, T> column, String header) {
        super.getTable().addColumn(column, header);
    }

    public void setEmptyTableWidget(Widget widget) {
        getTable().setEmptyTableWidget(widget);
    }

}
