package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.shared.TenantDTO;

public class TenantListDataProvider extends AbstractDataProvider<TenantDTO> {
    private UserManagementServiceAsync userManagementService;
    
    public TenantListDataProvider(UserManagementServiceAsync userManagementService) {
       this.userManagementService = userManagementService;
    }
   
    @Override
    protected void onRangeChanged(final HasData<TenantDTO> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getTenantList(new AsyncCallback<Collection<TenantDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
   
            @Override
            public void onSuccess(Collection<TenantDTO> result) {
                List<TenantDTO> resultList = new ArrayList<>(result);
                List<TenantDTO> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; i < end && i < resultList.size(); i++) {
                    final TenantDTO e = resultList.get(i);
                    show.add(e);
                }
                updateRowData(start, show);
                updateRowCount(result.size(), true);
            }
        });
    }
   
    public void updateDisplays() {
        for (HasData<TenantDTO> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
}
