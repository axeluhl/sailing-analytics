package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sap.sse.security.shared.AccessControlListAnnotationDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class AccessControlListListDataProvider extends AbstractDataProvider<AccessControlListAnnotationDTO> {
    private UserManagementServiceAsync userManagementService;
    
    public AccessControlListListDataProvider(UserManagementServiceAsync userManagementService) {
       this.userManagementService = userManagementService;
    }
   
    @Override
    protected void onRangeChanged(final HasData<AccessControlListAnnotationDTO> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getAccessControlLists(new AsyncCallback<Collection<AccessControlListAnnotationDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
   
            @Override
            public void onSuccess(Collection<AccessControlListAnnotationDTO> result) {
                List<AccessControlListAnnotationDTO> resultList = new ArrayList<>(result);
                List<AccessControlListAnnotationDTO> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; i < end && i < resultList.size(); i++) {
                    final AccessControlListAnnotationDTO e = resultList.get(i);
                    show.add(e);
                }
                updateRowData(start, show);
                updateRowCount(result.size(), true);
            }
        });
    }
   
    public void updateDisplays() {
        for (HasData<AccessControlListAnnotationDTO> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
}
