package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class AccessControlListListDataProvider extends AbstractDataProvider<AccessControlListAnnotation> {
    private UserManagementServiceAsync userManagementService;
    
    public AccessControlListListDataProvider(UserManagementServiceAsync userManagementService) {
       this.userManagementService = userManagementService;
    }
   
    @Override
    protected void onRangeChanged(final HasData<AccessControlListAnnotation> display) {
        final Range range = display.getVisibleRange();
        userManagementService.getAccessControlLists(new AsyncCallback<Collection<AccessControlListAnnotation>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
   
            @Override
            public void onSuccess(Collection<AccessControlListAnnotation> result) {
                List<AccessControlListAnnotation> resultList = new ArrayList<>(result);
                List<AccessControlListAnnotation> show = new ArrayList<>();
                int start = range.getStart();
                int end = range.getStart() + range.getLength();
                for (int i = start; i < end && i < resultList.size(); i++) {
                    final AccessControlListAnnotation e = resultList.get(i);
                    show.add(e);
                }
                updateRowData(start, show);
                updateRowCount(result.size(), true);
            }
        });
    }
   
    public void updateDisplays() {
        for (HasData<AccessControlListAnnotation> hd : getDataDisplays()) {
            onRangeChanged(hd);
        }
    }
}
