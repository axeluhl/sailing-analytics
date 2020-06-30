package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RemoteServerInstancesManagementTableWrapper extends
        TableWrapper<RemoteSailingServerReferenceDTO, RefreshableMultiSelectionModel<RemoteSailingServerReferenceDTO>, StringMessages, CellTableWithCheckboxResources> {
    public RemoteServerInstancesManagementTableWrapper(StringMessages stringMessages, ErrorReporter errorReporter,
            ListDataProvider<RemoteSailingServerReferenceDTO> dataProvider, CellTableWithCheckboxResources tableResources) {
        super(stringMessages, errorReporter, /* multiSelection */ true, /* pager */ true,
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
                }, tableResources);
        registerSelectionModelOnNewDataProvider(dataProvider);
    }
}
